package vhdt.wiis.app.calendar

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    
    val tasks = mutableStateOf<List<ScheduleItem>>(emptyList())
    // Cache: Map<LocalDate, List<ScheduleItem>> để tránh tính toán lặp lại
    private var tasksByDateCache = mutableMapOf<LocalDate, List<ScheduleItem>>()
    private var cachedTasksList: List<ScheduleItem> = emptyList()

    init {
        // Load schedule items asynchronously to avoid blocking main thread
        viewModelScope.launch {
            loadScheduleItems()
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

    fun hasTasksOnDate(date: LocalDate): Boolean {
        updateTasksCache()
        return tasksByDateCache.containsKey(date)
    }

    fun getTasksForDate(date: LocalDate): List<ScheduleItem> {
        updateTasksCache()
        return tasksByDateCache[date] ?: emptyList()
    }
}
