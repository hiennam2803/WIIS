package vhdt.wiis.app.data

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vhdt.wiis.data.local.PluginLoader
import vhdt.wiis.data.remote.ScheduleApiClient
import vhdt.wiis.data.repository.ScheduleRepository
import vhdt.wiis.model.SyncState
import vhdt.wiis.model.SchoolPlugin
import vhdt.wiis.scheduler.AlarmScheduler

class DataVM(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext
    private val repository = ScheduleRepository(
        context,
        ScheduleApiClient,
        PluginLoader
    )

    private val prefs = getApplication<Application>()
        .getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    private val _plugins = mutableStateOf<List<SchoolPlugin>>(emptyList())
    val plugins get() = _plugins.value

    val pluginCount = mutableStateOf(0)
    val eventCount = mutableStateOf(0)
    val alarmCount = mutableStateOf(0)

    val syncWeeks = mutableStateOf(
        prefs.getInt("syncWeeks", 4)
    )

    val syncState = mutableStateOf<SyncState>(SyncState.Idle)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val installedPlugins = repository.getInstalledPlugins()
            val savedItems = repository.getSavedScheduleItems()
            
            val futureAlarmsCount = withContext(Dispatchers.Default) {
                savedItems.count { it.startTime > System.currentTimeMillis() }
            }
            
            withContext(Dispatchers.Main) {
                _plugins.value = installedPlugins
                pluginCount.value = installedPlugins.size
                eventCount.value = savedItems.size
                alarmCount.value = futureAlarmsCount
                
                // Schedule daily summary alarm on app startup
                AlarmScheduler.scheduleDailySummaryAlarm(context)
                
                // Reschedule individual alarms for saved items
                if (savedItems.isNotEmpty()) {
                    AlarmScheduler.scheduleAllAlarms(
                        context,
                        savedItems,
                        prefs.getInt("notifyMinutesBefore", 60)
                    )
                }
            }
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    fun syncSchedule() {
        syncState.value = SyncState.Loading
        
        viewModelScope.launch {
            val activePlugin = repository.getInstalledPlugins().firstOrNull()
            
            if (activePlugin == null) {
                syncState.value = SyncState.NoPlugin
                return@launch
            }
            
            val result = repository.fetchSchedule(activePlugin, syncWeeks.value)
            syncState.value = result

            if (result is SyncState.Error && result.message == "AUTH_REQUIRED") {
                // JWT expired — token already cleared in repository
                // LaunchedEffect in DataScreen will catch this and open WebView
                return@launch
            }

            if (result is SyncState.Success) {
                repository.saveScheduleItems(result.items)
                eventCount.value = result.items.size
                AlarmScheduler.scheduleAllAlarms(
                    context,
                    result.items,
                    prefs.getInt("notifyMinutesBefore", 60)
                )
                AlarmScheduler.scheduleDailySummaryAlarm(context)
                alarmCount.value = result.items
                    .count { it.startTime > System.currentTimeMillis() }
            }
        }
    }

    fun syncGoogleCalendar() {
        syncState.value = SyncState.Loading
        // TODO: Google Calendar — Phase 2
    }
    
    fun importPluginJson(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val plugin = PluginLoader.loadPluginFromUri(context, uri)
            if (plugin != null) {
                repository.addPlugin(plugin)
                val updatedPlugins = repository.getInstalledPlugins()
                
                withContext(Dispatchers.Main) {
                    _plugins.value = updatedPlugins
                    pluginCount.value = updatedPlugins.size
                }
            } else {
                withContext(Dispatchers.Main) {
                    syncState.value = SyncState.Error("Failed to import plugin")
                }
            }
        }
    }

    fun getJwtToken(): String? {
        val plugin = repository.getInstalledPlugins().firstOrNull() ?: return null
        return repository.getJwtToken(plugin.id)
    }

    fun saveJwtToken(token: String) {
        val plugin = repository.getInstalledPlugins().firstOrNull() ?: return
        repository.saveJwtToken(plugin.id, token)
    }
    
    fun clearJwtToken() {
        val plugin = repository.getInstalledPlugins().firstOrNull() ?: return
        repository.clearJwtToken(plugin.id)
    }

    fun setSyncWeeks(weeks: Int) {
        syncWeeks.value = weeks
        prefs.edit().putInt("syncWeeks", weeks).apply()
    }
}