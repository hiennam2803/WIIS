package vhdt.wiis.app.calendar

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.compose.runtime.mutableStateOf
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

    init {
        loadScheduleItems()
    }

    private fun loadScheduleItems() {
        tasks.value = repository.getSavedScheduleItems()
    }

    fun refreshTasks() {
        tasks.value = repository.getSavedScheduleItems()
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
        return tasks.value.any { task ->
            Instant.ofEpochMilli(task.startTime)
                .atZone(ZoneId.systemDefault())
                .toLocalDate() == date
        }
    }

    fun getTasksForDate(date: LocalDate): List<ScheduleItem> {
        return tasks.value.filter { task ->
            Instant.ofEpochMilli(task.startTime)
                .atZone(ZoneId.systemDefault())
                .toLocalDate() == date
        }
    }
}
