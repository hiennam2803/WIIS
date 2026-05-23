package vhdt.wiis.data.remote

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import vhdt.wiis.model.ScheduleItem
import vhdt.wiis.model.ScheduleType
import vhdt.wiis.model.SchoolPlugin
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object ScheduleApiClient {
    private const val TAG = "UthApiClient"
    private val json = Json { ignoreUnknownKeys = true }

    // Bảng tiết → giờ chuẩn UTH HCMC
    private fun tietToTime(tiet: Int): String = when (tiet) {
        1  -> "07:00"; 2  -> "07:55"; 3  -> "08:50"
        4  -> "09:45"; 5  -> "10:40"; 6  -> "11:35"
        7  -> "12:30"; 8  -> "13:25"; 9  -> "14:20"
        10 -> "15:15"; 11 -> "16:10"; 12 -> "17:05"
        else -> "07:00"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchWeekSchedule(
        plugin: SchoolPlugin,
        jwtToken: String,
        date: String
    ): List<ScheduleItem> = withContext(Dispatchers.IO) {
        return@withContext try {
            val url = URL("${plugin.scheduleEndpoint}?date=$date")
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                instanceFollowRedirects = false
                setRequestProperty("Authorization", "Bearer $jwtToken")
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
                )
                connectTimeout = 10000
                readTimeout = 10000
            }

            val responseCode = connection.responseCode
            Log.d(TAG, "Response code: $responseCode")

            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "API error: $responseCode")
                connection.disconnect()
                return@withContext emptyList()
            }

            val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()

            // Nếu server trả HTML thay vì JSON → token hết hạn
            if (responseBody.trim().startsWith("<!doctype", ignoreCase = true) ||
                responseBody.trim().startsWith("<html", ignoreCase = true)
            ) {
                Log.e(TAG, "Server trả về HTML – token có thể hết hạn")
                return@withContext emptyList()
            }

            // Parse JSON: hỗ trợ cả dạng array thẳng lẫn dạng object bọc ngoài
            val rootElement = json.parseToJsonElement(responseBody)
            val jsonArray = when {
                // Có responseBodyKey trong config (ví dụ "body") → lấy field đó
                plugin.responseBodyKey != null -> {
                    rootElement.jsonObject[plugin.responseBodyKey]?.jsonArray
                        ?: run {
                            Log.e(TAG, "Không tìm thấy key '${plugin.responseBodyKey}' trong response")
                            return@withContext emptyList()
                        }
                }
                // Không có config → thử lấy field "body" nếu là object
                rootElement is kotlinx.serialization.json.JsonObject -> {
                    rootElement.jsonObject["body"]?.jsonArray
                        ?: rootElement.jsonObject["data"]?.jsonArray
                        ?: run {
                            Log.e(TAG, "Response là object nhưng không có field 'body' hay 'data'")
                            return@withContext emptyList()
                        }
                }
                // Response là array thẳng
                else -> rootElement.jsonArray
            }

            jsonArray.mapNotNull { element ->
                try {
                    val item = element.jsonObject

                    val title = item[plugin.jsonMapping.title]?.jsonPrimitive?.content
                        ?: return@mapNotNull null
                    val location = item[plugin.jsonMapping.location]?.jsonPrimitive?.content ?: ""
                    val dateStr = item[plugin.jsonMapping.date]?.jsonPrimitive?.content
                        ?: return@mapNotNull null
                    val noteKey = plugin.jsonMapping.note ?: "coSoToDisplay"
                    val note = item[noteKey]?.jsonPrimitive?.content ?: ""

                    // Xử lý startTime / endTime: có thể là "HH:mm" hoặc số tiết
                    val startRaw = item[plugin.jsonMapping.startTime]?.jsonPrimitive?.content
                        ?: return@mapNotNull null
                    val endRaw = item[plugin.jsonMapping.endTime]?.jsonPrimitive?.content ?: ""

                    val startTimeStr = if (plugin.timeFormat == "tiet") {
                        tietToTime(startRaw.toIntOrNull() ?: 0)
                    } else startRaw

                    val endTimeStr = if (plugin.timeFormat == "tiet") {
                        // Cộng thêm 45 phút cho tiết cuối
                        val endTiet = endRaw.toIntOrNull() ?: 0
                        addMinutes(tietToTime(endTiet), 45)
                    } else endRaw

                    val startTimeMs = convertToEpochMs(dateStr, startTimeStr)
                    val endTimeMs = convertToEpochMs(dateStr, endTimeStr)
                    val id = generateId(title, dateStr, startTimeStr)

                    ScheduleItem(
                        id = id,
                        title = title,
                        location = location,
                        note = note,
                        startTime = startTimeMs,
                        endTime = endTimeMs,
                        type = ScheduleType.UNIVERSITY
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing schedule item: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching schedule: ${e.message}")
            emptyList()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addMinutes(timeStr: String, minutes: Long): String {
        return try {
            val time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"))
            time.plusMinutes(minutes).format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            timeStr
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun convertToEpochMs(dateStr: String, timeStr: String): Long {
        return try {
            val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            val date = LocalDate.parse(dateStr, dateFormatter)
            val time = LocalTime.parse(timeStr, timeFormatter)
            date.atTime(time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (e: Exception) {
            Log.e(TAG, "Error converting date/time: $dateStr $timeStr — ${e.message}")
            0L
        }
    }

    private fun generateId(title: String, date: String, time: String): String {
        val input = "$title$date$time"
        val md = MessageDigest.getInstance("MD5")
        val messageDigest = md.digest(input.toByteArray())
        return messageDigest.joinToString("") { "%02x".format(it) }
    }
}