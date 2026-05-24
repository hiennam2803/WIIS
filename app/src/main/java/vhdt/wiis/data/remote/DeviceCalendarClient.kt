package vhdt.wiis.data.remote

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.CalendarContract
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import android.Manifest
import vhdt.wiis.model.ScheduleItem
import vhdt.wiis.model.ScheduleType
import java.util.TimeZone

object DeviceCalendarClient {
    private const val TAG = "DeviceCalendarClient"
    private const val WIIS_TAG = "[WIIS]"

    @RequiresApi(Build.VERSION_CODES.O)
    fun getAvailableCalendars(context: Context): List<DeviceCalendar> {
        if (!hasPermission(context, Manifest.permission.READ_CALENDAR)) {
            Log.w(TAG, "Permission READ_CALENDAR not granted")
            return emptyList()
        }

        return try {
            val contentResolver = context.contentResolver
            val calendars = mutableListOf<DeviceCalendar>()

            val projection = arrayOf(
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                CalendarContract.Calendars.ACCOUNT_NAME,
                CalendarContract.Calendars.CALENDAR_COLOR
            )

            val cursor = contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                null,
                null,
                null
            )

            cursor?.use {
                val idIndex = it.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
                val nameIndex = it.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
                val accountIndex = it.getColumnIndexOrThrow(CalendarContract.Calendars.ACCOUNT_NAME)
                val colorIndex = it.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_COLOR)

                while (it.moveToNext()) {
                    calendars.add(
                        DeviceCalendar(
                            id = it.getLong(idIndex),
                            name = it.getString(nameIndex),
                            accountName = it.getString(accountIndex),
                            color = it.getInt(colorIndex)
                        )
                    )
                }
            }

            Log.d(TAG, "Found ${calendars.size} calendars")
            calendars
        } catch (e: Exception) {
            Log.e(TAG, "Error getting available calendars: ${e.message}", e)
            emptyList()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun exportToDeviceCalendar(
        context: Context,
        items: List<ScheduleItem>,
        calendarId: Long
    ): ExportResult {
        if (!hasPermission(context, Manifest.permission.WRITE_CALENDAR)) {
            Log.w(TAG, "Permission WRITE_CALENDAR not granted")
            return ExportResult.PermissionDenied
        }

        return try {
            val contentResolver = context.contentResolver
            var exportedCount = 0

            for (item in items) {
                val values = ContentValues().apply {
                    put(CalendarContract.Events.CALENDAR_ID, calendarId)
                    put(CalendarContract.Events.TITLE, item.title)
                    put(CalendarContract.Events.DESCRIPTION, "$WIIS_TAG ${item.note}")
                    put(CalendarContract.Events.EVENT_LOCATION, item.location)
                    put(CalendarContract.Events.DTSTART, item.startTime)
                    put(CalendarContract.Events.DTEND, item.endTime)
                    put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                    put(CalendarContract.Events.ACCESS_LEVEL, CalendarContract.Events.ACCESS_PRIVATE)
                    put(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                }

                try {
                    val eventUri = contentResolver.insert(
                        CalendarContract.Events.CONTENT_URI,
                        values
                    )
                    if (eventUri != null) {
                        exportedCount++
                        Log.d(TAG, "Exported event: ${item.title} to calendar $calendarId")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error exporting event '${item.title}': ${e.message}")
                }
            }

            Log.d(TAG, "Export completed: $exportedCount/${items.size} events exported")
            ExportResult.Success(exportedCount)
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting to device calendar: ${e.message}", e)
            ExportResult.Error(e.message ?: "Unknown error")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun importFromDeviceCalendar(
        context: Context,
        calendarId: Long,
        startMs: Long,
        endMs: Long
    ): List<ScheduleItem> {
        if (!hasPermission(context, Manifest.permission.READ_CALENDAR)) {
            Log.w(TAG, "Permission READ_CALENDAR not granted")
            return emptyList()
        }

        return try {
            val contentResolver = context.contentResolver
            val scheduleItems = mutableListOf<ScheduleItem>()

            val projection = arrayOf(
                CalendarContract.Events._ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DESCRIPTION,
                CalendarContract.Events.EVENT_LOCATION,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND
            )

            val selection = "${CalendarContract.Events.CALENDAR_ID} = ? AND " +
                    "${CalendarContract.Events.DTSTART} >= ? AND " +
                    "${CalendarContract.Events.DTEND} <= ?"
            val selectionArgs = arrayOf(
                calendarId.toString(),
                startMs.toString(),
                endMs.toString()
            )

            val cursor = contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )

            cursor?.use {
                val idIndex = it.getColumnIndexOrThrow(CalendarContract.Events._ID)
                val titleIndex = it.getColumnIndexOrThrow(CalendarContract.Events.TITLE)
                val descIndex = it.getColumnIndexOrThrow(CalendarContract.Events.DESCRIPTION)
                val locationIndex = it.getColumnIndexOrThrow(CalendarContract.Events.EVENT_LOCATION)
                val startIndex = it.getColumnIndexOrThrow(CalendarContract.Events.DTSTART)
                val endIndex = it.getColumnIndexOrThrow(CalendarContract.Events.DTEND)

                while (it.moveToNext()) {
                    try {
                        val id = it.getLong(idIndex).toString()
                        val title = it.getString(titleIndex)
                        val description = it.getString(descIndex) ?: ""
                        val location = it.getString(locationIndex) ?: ""
                        val startTime = it.getLong(startIndex)
                        val endTime = it.getLong(endIndex)

                        val note = description.replace(WIIS_TAG, "").trim()

                        scheduleItems.add(
                            ScheduleItem(
                                id = id,
                                title = title,
                                location = location,
                                note = note,
                                startTime = startTime,
                                endTime = endTime,
                                type = ScheduleType.UNIVERSITY
                            )
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing calendar event: ${e.message}")
                    }
                }
            }

            Log.d(TAG, "Imported ${scheduleItems.size} events from device calendar")
            scheduleItems
        } catch (e: Exception) {
            Log.e(TAG, "Error importing from device calendar: ${e.message}", e)
            emptyList()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun deleteExportedEvents(context: Context, calendarId: Long) {
        if (!hasPermission(context, Manifest.permission.WRITE_CALENDAR)) {
            Log.w(TAG, "Permission WRITE_CALENDAR not granted for delete operation")
            return
        }

        try {
            val contentResolver = context.contentResolver

            val selection = "${CalendarContract.Events.CALENDAR_ID} = ? AND " +
                    "${CalendarContract.Events.DESCRIPTION} LIKE ?"
            val selectionArgs = arrayOf(
                calendarId.toString(),
                "%$WIIS_TAG%"
            )

            contentResolver.delete(
                CalendarContract.Events.CONTENT_URI,
                selection,
                selectionArgs
            )

            Log.d(TAG, "Deleted exported events from calendar $calendarId")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting exported events: ${e.message}", e)
        }
    }

    private fun hasPermission(context: Context, permission: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ContextCompat.checkSelfPermission(context, permission) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}

data class DeviceCalendar(
    val id: Long,
    val name: String,
    val accountName: String,
    val color: Int
)

sealed class ExportResult {
    data class Success(val exportedCount: Int) : ExportResult()
    data class Error(val message: String) : ExportResult()
    object PermissionDenied : ExportResult()
}
