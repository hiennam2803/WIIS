package vhdt.wiis.app.calendar

import android.app.Application
import android.content.Context
import android.os.Build
import android.provider.CalendarContract
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.collect
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import vhdt.wiis.data.local.PluginLoader
import vhdt.wiis.data.remote.ScheduleApiClient
import vhdt.wiis.data.repository.ScheduleRepository
import vhdt.wiis.model.ScheduleItem
import android.content.Intent
import android.provider.AlarmClock
import android.widget.Toast

@RequiresApi(Build.VERSION_CODES.O)
class CalendarVM(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val repository = ScheduleRepository(
        context, ScheduleApiClient, PluginLoader
    )

    val today = LocalDate.now()
    val selectedDate = mutableStateOf(today)
    val displayMonth = mutableStateOf(YearMonth.now())
    
    // Track the last selected date for debugging state issues
    private var lastLoggedSelectedDate: LocalDate? = null
    val expandedItems = mutableStateOf(setOf<Int>())
    val expandedDeviceItems = mutableStateOf(setOf<Int>())
    
    val tasks = mutableStateOf<List<ScheduleItem>>(emptyList())
    val deviceEvents = mutableStateOf<List<DeviceScheduleItem>>(emptyList())
    val eventNotificationSettings = mutableStateOf<Map<String, EventNotificationSettings>>(
        loadEventSettings()
    )
    // Cache: Map<LocalDate, List<ScheduleItem>> để tránh tính toán lặp lại
    private var tasksByDateCache = mutableMapOf<LocalDate, List<ScheduleItem>>()
    private var cachedTasksList: List<ScheduleItem> = emptyList()

    init {
        viewModelScope.launch {
            repository.scheduleItems.collect { items ->
                tasks.value = items
                updateTasksCache()
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.getSavedScheduleItems()
        }
    }

    private fun loadEventSettings(): Map<String, EventNotificationSettings> {
        return try {
            val json = Json { ignoreUnknownKeys = true }
            val str = context.getSharedPreferences("event_settings", Context.MODE_PRIVATE)
                .getString("settings", null) ?: return emptyMap()
            json.decodeFromString(str)
        } catch (e: Exception) { 
            emptyMap() 
        }
    }

    fun saveEventSettings(eventId: String, settings: EventNotificationSettings, deviceEventTitle: String? = null, deviceEventStartTime: Long? = null) {
        val current = eventNotificationSettings.value.toMutableMap()
        current[eventId] = settings
        eventNotificationSettings.value = current
        // Save to prefs
        val json = Json { ignoreUnknownKeys = true }
        context.getSharedPreferences("event_settings", Context.MODE_PRIVATE)
            .edit().putString("settings", json.encodeToString(current)).apply()
        // Schedule or cancel using device Clock app (AlarmClock.ACTION_SET_ALARM)
        if (settings.alarmEnabled) {
            // Determine startTime and title
            var title: String? = deviceEventTitle
            var startMs: Long? = deviceEventStartTime

            val item = tasks.value.find { it.id == eventId }
            if (item != null) {
                title = item.title
                startMs = item.startTime
            }

            // If we still don't have startMs, try deviceEvents cache
            if (startMs == null) {
                val deviceItem = deviceEvents.value.find { it.id == eventId }
                if (deviceItem != null) {
                    title = deviceItem.title
                    startMs = deviceItem.startTime
                }
            }

            if (startMs != null) {
                try {
                    val triggerMs = startMs - settings.minutesBefore * 60 * 1000L
                    val zdt = Instant.ofEpochMilli(triggerMs).atZone(ZoneId.systemDefault())
                    val hour = zdt.hour
                    val minute = zdt.minute

                    val alarmIntent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                        putExtra(AlarmClock.EXTRA_MESSAGE, title ?: "Alarm")
                        putExtra(AlarmClock.EXTRA_HOUR, hour)
                        putExtra(AlarmClock.EXTRA_MINUTES, minute)
                        putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(alarmIntent)
                    Log.d("CalendarVM", "Set system alarm for $eventId at $hour:$minute (message=${title})")
                } catch (e: Exception) {
                    Log.e("CalendarVM", "Error setting system alarm for $eventId", e)
                }
            } else {
                Log.w("CalendarVM", "No start time found for event $eventId; cannot set alarm")
            }
        } else {
            // Hướng dẫn user tự xóa trong app Đồng Hồ
            try {
                // Determine title for user message
                var title: String? = deviceEventTitle
                val item = tasks.value.find { it.id == eventId }
                if (item != null) title = item.title
                if (title == null) {
                    val deviceItem = deviceEvents.value.find { it.id == eventId }
                    if (deviceItem != null) title = deviceItem.title
                }

                Toast.makeText(
                    context,
                    "Vui lòng xóa báo thức '${title ?: "Alarm"}' trong app Đồng Hồ",
                    Toast.LENGTH_LONG
                ).show()
                val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e("CalendarVM", "Error opening clock app", e)
            }
        }
    }

    fun getEventSettings(eventId: String): EventNotificationSettings {
        return eventNotificationSettings.value[eventId] ?: EventNotificationSettings()
    }

    private suspend fun loadScheduleItems() {
        withContext(Dispatchers.IO) {
            val items = repository.getSavedScheduleItems()
            withContext(Dispatchers.Main) {
                tasks.value = items
                updateTasksCache()
            }
        }
    }

    fun refreshTasks() {
        viewModelScope.launch {
            val items = withContext(Dispatchers.IO) {
                repository.getSavedScheduleItems()
            }
            tasks.value = items
            updateTasksCache()
        }
    }

    private fun updateTasksCache() {
        // Chỉ tính toán nếu tasks thay đổi
        if (cachedTasksList != tasks.value) {
            cachedTasksList = tasks.value
            tasksByDateCache.clear()
            
            // Build cache một lần
            tasks.value.forEach { task ->
                val date = Instant.ofEpochMilli(task.startTime)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                val existing = tasksByDateCache[date] ?: emptyList()
                tasksByDateCache[date] = existing + task
            }
        }
    }

    fun selectDate(date: LocalDate) {
        // Guard against null or invalid dates
        if (date.year < 2000 || date.year > 2100) {
            Log.w("CalendarVM", "selectDate called with invalid year: ${date.year}")
            return
        }
        
        selectedDate.value = date
        
        // Log state change with context
        if (lastLoggedSelectedDate != date) {
            Log.d("CalendarVM", "✓ selectedDate UPDATED to: ${date.toStandardFormat()} (${date.dayOfWeek}, month=${date.monthValue}, day=${date.dayOfMonth})")
            lastLoggedSelectedDate = date
        }
    }

    fun goToPreviousMonth() {
        displayMonth.value = displayMonth.value.minusMonths(1)
    }

    fun goToNextMonth() {
        displayMonth.value = displayMonth.value.plusMonths(1)
    }

    fun toggleTaskExpanded(index: Int) {
        expandedItems.value = if (expandedItems.value.contains(index)) {
            expandedItems.value - index
        } else {
            expandedItems.value + index
        }
    }

    fun toggleDeviceEventExpanded(index: Int) {
        expandedDeviceItems.value = if (expandedDeviceItems.value.contains(index)) {
            expandedDeviceItems.value - index
        } else {
            expandedDeviceItems.value + index
        }
    }

    fun hasTasksOnDate(date: LocalDate): Boolean {
        updateTasksCache()
        return tasksByDateCache.containsKey(date)
    }

    fun getTasksForDate(date: LocalDate): List<ScheduleItem> {
        updateTasksCache()
        return tasksByDateCache[date] ?: emptyList()
    }

    fun hasAlarmOnDate(date: LocalDate): Boolean {
        return eventNotificationSettings.value.any { (eventId, settings) ->
            if (!settings.alarmEnabled) return@any false
            
            // Check school events first
            val schoolItem = tasks.value.find { it.id == eventId }
            if (schoolItem != null) {
                val eventDate = Instant.ofEpochMilli(schoolItem.startTime)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                val matches = eventDate == date
                Log.d("CalendarVM", "Alarm check (school): '${schoolItem.title}' at ${eventDate.toStandardFormat()} vs ${date.toStandardFormat()} -> $matches")
                return@any matches
            }
            
            // Check device events
            val deviceItem = deviceEvents.value.find { it.id == eventId }
            if (deviceItem != null) {
                val eventDate = Instant.ofEpochMilli(deviceItem.startTime)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                val matches = eventDate == date
                Log.d("CalendarVM", "Alarm check (device): '${deviceItem.title}' at ${eventDate.toStandardFormat()} vs ${date.toStandardFormat()} -> $matches")
                return@any matches
            }
            
            return@any false
        }
    }

    fun loadDeviceCalendarEvents(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val events = mutableListOf<DeviceScheduleItem>()
            val startMs = LocalDate.now().minusMonths(1)
                .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endMs = LocalDate.now().plusMonths(2)
                .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            try {
                Log.d("CalendarVM", "Starting to load device calendar events")
                Log.d("CalendarVM", "Date range: $startMs to $endMs")
                
                val projection = arrayOf(
                    CalendarContract.Events._ID,
                    CalendarContract.Events.TITLE,
                    CalendarContract.Events.DTSTART,
                    CalendarContract.Events.DTEND,
                    CalendarContract.Events.CALENDAR_DISPLAY_NAME,
                    CalendarContract.Events.DISPLAY_COLOR,
                    CalendarContract.Events.EVENT_COLOR
                )
                
                // Find events: DTSTART < endMs AND DTEND > startMs
                val selection = "${CalendarContract.Events.DTSTART} < ? AND ${CalendarContract.Events.DTEND} > ?"
                val selectionArgs = arrayOf(endMs.toString(), startMs.toString())
                
                Log.d("CalendarVM", "Querying with selection: $selection")
                Log.d("CalendarVM", "Selection args: startMs=$startMs, endMs=$endMs")
                
                val cursor = context.contentResolver.query(
                    CalendarContract.Events.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    "${CalendarContract.Events.DTSTART} ASC"
                )
                
                Log.d("CalendarVM", "Cursor result: ${cursor?.count} events found")
                
                cursor?.use { c ->
                    val idIndex = c.getColumnIndex(CalendarContract.Events._ID)
                    val titleIndex = c.getColumnIndex(CalendarContract.Events.TITLE)
                    val dtstartIndex = c.getColumnIndex(CalendarContract.Events.DTSTART)
                    val dtendIndex = c.getColumnIndex(CalendarContract.Events.DTEND)
                    val calendarNameIndex = c.getColumnIndex(CalendarContract.Events.CALENDAR_DISPLAY_NAME)
                    val displayColorIndex = c.getColumnIndex(CalendarContract.Events.DISPLAY_COLOR)
                    val eventColorIndex = c.getColumnIndex(CalendarContract.Events.EVENT_COLOR)
                    
                    Log.d("CalendarVM", "Column indices - ID:$idIndex Title:$titleIndex Start:$dtstartIndex End:$dtendIndex CalName:$calendarNameIndex DispColor:$displayColorIndex EventColor:$eventColorIndex")
                    
                    while (c.moveToNext()) {
                        try {
                            val id = if (idIndex >= 0) c.getLong(idIndex).toString() else System.currentTimeMillis().toString()
                            val title = if (titleIndex >= 0 && !c.isNull(titleIndex)) c.getString(titleIndex) else "Event"
                            val startTime = if (dtstartIndex >= 0) c.getLong(dtstartIndex) else continue
                            val endTime = if (dtendIndex >= 0) c.getLong(dtendIndex) else startTime + 3600000
                            val calendarName = if (calendarNameIndex >= 0 && !c.isNull(calendarNameIndex)) c.getString(calendarNameIndex) else ""
                            
                            // Try EVENT_COLOR first, then DISPLAY_COLOR, fallback to default blue
                            val color = when {
                                eventColorIndex >= 0 && !c.isNull(eventColorIndex) -> c.getInt(eventColorIndex)
                                displayColorIndex >= 0 && !c.isNull(displayColorIndex) -> c.getInt(displayColorIndex)
                                else -> 0xFF1856BB.toInt()
                            }
                            
                            events.add(DeviceScheduleItem(
                                id = id,
                                title = title,
                                startTime = startTime,
                                endTime = endTime,
                                calendarName = calendarName,
                                color = color
                            ))
                            
                            Log.d("CalendarVM", "Added event: $title at $startTime")
                        } catch (e: Exception) {
                            Log.e("CalendarVM", "Error reading event row", e)
                        }
                    }
                }
                
                Log.d("CalendarVM", "Total events loaded: ${events.size}")
            } catch (e: SecurityException) {
                Log.e("CalendarVM", "Permission denied for calendar", e)
            } catch (e: Exception) {
                Log.e("CalendarVM", "Error loading calendar events", e)
            }
            
            withContext(Dispatchers.Main) {
                deviceEvents.value = events
                Log.d("CalendarVM", "Device events state updated with ${events.size} events")
            }
        }
    }

    fun getDeviceEventsForDate(date: LocalDate): List<DeviceScheduleItem> {
        val filtered = deviceEvents.value.filter { event ->
            val eventDate = Instant.ofEpochMilli(event.startTime)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            val matches = eventDate == date
            Log.d("CalendarVM", "Filter event: '${event.title}' at ${eventDate.toStandardFormat()} vs ${date.toStandardFormat()} -> $matches")
            matches
        }
        Log.d("CalendarVM", "getDeviceEventsForDate(${date.toStandardFormat()}) -> ${filtered.size} events from total ${deviceEvents.value.size}")
        return filtered
    }

    fun debugCheckCalendars(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("CalendarVM", "=== DEBUG: Checking available calendars ===")
                
                val calendarProjection = arrayOf(
                    CalendarContract.Calendars._ID,
                    CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                    CalendarContract.Calendars.CALENDAR_COLOR,
                    CalendarContract.Calendars.OWNER_ACCOUNT
                )
                
                val calendarCursor = context.contentResolver.query(
                    CalendarContract.Calendars.CONTENT_URI,
                    calendarProjection,
                    null,
                    null,
                    null
                )
                
                Log.d("CalendarVM", "Found ${calendarCursor?.count} calendars")
                
                calendarCursor?.use {
                    val idIndex = it.getColumnIndex(CalendarContract.Calendars._ID)
                    val nameIndex = it.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
                    val colorIndex = it.getColumnIndex(CalendarContract.Calendars.CALENDAR_COLOR)
                    val accountIndex = it.getColumnIndex(CalendarContract.Calendars.OWNER_ACCOUNT)
                    
                    while (it.moveToNext()) {
                        val id = if (idIndex >= 0) it.getLong(idIndex) else 0L
                        val name = if (nameIndex >= 0 && !it.isNull(nameIndex)) it.getString(nameIndex) else "Unknown"
                        val color = if (colorIndex >= 0 && !it.isNull(colorIndex)) it.getInt(colorIndex) else 0
                        val account = if (accountIndex >= 0 && !it.isNull(accountIndex)) it.getString(accountIndex) else "Unknown"
                        
                        Log.d("CalendarVM", "Calendar: ID=$id, Name=$name, Color=$color, Account=$account")
                    }
                }
                
                Log.d("CalendarVM", "=== Total events in database: ${deviceEvents.value.size} ===")
            } catch (e: Exception) {
                Log.e("CalendarVM", "Error debugging calendars", e)
            }
        }
    }

    @Serializable
    data class EventNotificationSettings(
        val notificationEnabled: Boolean = true,
        val alarmEnabled: Boolean = false,
        val minutesBefore: Int = 60
    )

    data class DeviceScheduleItem(
        val id: String,
        val title: String,
        val startTime: Long,
        val endTime: Long,
        val calendarName: String,
        val color: Int
    )
}

// Extension function for standard date formatting
fun LocalDate.toStandardFormat(): String {
    return this.toString()  // yyyy-MM-dd format (ISO 8601)
}
