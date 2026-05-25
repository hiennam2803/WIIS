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
        // Load data asynchronously to avoid blocking main thread
        viewModelScope.launch {
            loadSavedData()
        }
    }
    
    private suspend fun loadSavedData() {
        withContext(Dispatchers.IO) {
            val installedPlugins = repository.getInstalledPlugins()
            val savedItems = repository.getSavedScheduleItems()
            
            withContext(Dispatchers.Main) {
                _plugins.value = installedPlugins
                pluginCount.value = installedPlugins.size
                eventCount.value = savedItems.size
                alarmCount.value = savedItems.count { it.startTime > System.currentTimeMillis() }
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

            if (result is SyncState.Success) {
                repository.saveScheduleItems(result.items)
                eventCount.value = result.items.size

                // THÊM 2 dòng này
                AlarmScheduler.scheduleAllAlarms(
                    context,
                    result.items,
                    prefs.getInt("notifyMinutesBefore", 60)
                )
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
        val plugin = PluginLoader.loadPluginFromUri(context, uri)
        
        if (plugin != null) {
            _plugins.value = _plugins.value + plugin
            pluginCount.value = _plugins.value.size
            Log.d("DataVM", "Plugin imported successfully: ${plugin.id}")
        } else {
            syncState.value = SyncState.Error("Failed to import plugin from URI")
            Log.e("DataVM", "Failed to import plugin from URI")
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

    fun setSyncWeeks(weeks: Int) {
        syncWeeks.value = weeks
        prefs.edit().putInt("syncWeeks", weeks).apply()
    }
}