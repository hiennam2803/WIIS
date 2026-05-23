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
                setRequestProperty("Authorization", "Bearer $jwtToken")
                setRequestProperty("Content-Type", "application/json")
                connectTimeout = 10000
                readTimeout = 10000
            }
            
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "API error: ${connection.responseCode}")
                connection.disconnect()
                return@withContext emptyList()
            }
            
            val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()
            
            val jsonArray = json.parseToJsonElement(responseBody).jsonArray
            jsonArray.mapNotNull { element ->
                try {
                    val item = element.jsonObject
                    val title = item[plugin.jsonMapping.title]?.jsonPrimitive?.content ?: return@mapNotNull null
                    val location = item[plugin.jsonMapping.location]?.jsonPrimitive?.content ?: ""
                    val startTimeStr = item[plugin.jsonMapping.startTime]?.jsonPrimitive?.content ?: return@mapNotNull null
                    val endTimeStr = item[plugin.jsonMapping.endTime]?.jsonPrimitive?.content ?: ""
                    val dateStr = item[plugin.jsonMapping.date]?.jsonPrimitive?.content ?: return@mapNotNull null
                    val note = item["coSoToDisplay"]?.jsonPrimitive?.content ?: ""
                    
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
                    Log.e(TAG, "Error parsing schedule item", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching schedule", e)
            emptyList()
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
            Log.e(TAG, "Error converting date/time: $dateStr $timeStr", e)
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
