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
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import vhdt.wiis.data.local.PluginLoader
import vhdt.wiis.data.remote.ScheduleApiClient
import vhdt.wiis.data.repository.ScheduleRepository
import vhdt.wiis.model.ScheduleItem

@RequiresApi(Build.VERSION_CODES.O)
class CalendarVM(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val repository = ScheduleRepository(
        context, ScheduleApiClient, PluginLoader
    )

    val today = LocalDate.now()
    val selectedDate = mutableStateOf(today)
    val displayMonth = mutableStateOf(YearMonth.now())
    val expandedItems = mutableStateOf(setOf<Int>())
    val expandedDeviceItems = mutableStateOf(setOf<Int>())
    
    val tasks = mutableStateOf<List<ScheduleItem>>(emptyList())
    val deviceEvents = mutableStateOf<List<DeviceScheduleItem>>(emptyList())
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
        selectedDate.value = date
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
            Log.d("CalendarVM", "Filter event: '${event.title}' at $eventDate vs $date -> $matches")
            matches
        }
        Log.d("CalendarVM", "getDeviceEventsForDate($date) -> ${filtered.size} events from total ${deviceEvents.value.size}")
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

    data class DeviceScheduleItem(
        val id: String,
        val title: String,
        val startTime: Long,
        val endTime: Long,
        val calendarName: String,
        val color: Int
    )
}
