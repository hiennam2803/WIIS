package vhdt.wiis.app.data

import android.Manifest
import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vhdt.wiis.data.local.PluginLoader
import vhdt.wiis.data.remote.ScheduleApiClient
import vhdt.wiis.data.remote.DeviceCalendarClient
import vhdt.wiis.data.repository.ScheduleRepository
import vhdt.wiis.model.SyncState
import vhdt.wiis.model.SchoolPlugin
// AlarmScheduler removed; using system Clock app via AlarmClock intents instead
import android.net.Uri

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
    val deviceCount = mutableStateOf(0)

    val syncWeeks = mutableStateOf(
        prefs.getInt("syncWeeks", 4)
    )

    val syncSchoolEnabled = mutableStateOf(
        prefs.getBoolean("syncSchoolEnabled", true)
    )
    val syncDeviceCalendarEnabled = mutableStateOf(
        prefs.getBoolean("syncDeviceCalendarEnabled", false)
    )

    val syncState = mutableStateOf<SyncState>(SyncState.Idle)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val installedPlugins = repository.getInstalledPlugins()
            val savedItems = repository.getSavedScheduleItems()
            
            val device = withContext(Dispatchers.Default) {
                // Count device calendar events in a reasonable window (1 month back to 2 months forward)
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        val startMs = java.time.LocalDate.now().minusMonths(1)
                            .atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                        val endMs = java.time.LocalDate.now().plusMonths(2)
                            .atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

                        val calendars = DeviceCalendarClient.getAvailableCalendars(context)
                        var count = 0
                        for (cal in calendars) {
                            try {
                                val items = DeviceCalendarClient.importFromDeviceCalendar(context, cal.id, startMs, endMs)
                                count += items.size
                            } catch (_: Exception) { }
                        }
                        count
                    } else {
                        0
                    }
                } catch (e: Exception) {
                    0
                }
            }
            
            withContext(Dispatchers.Main) {
                _plugins.value = installedPlugins
                pluginCount.value = installedPlugins.size
                eventCount.value = savedItems.size
                deviceCount.value = device
                
                // No longer scheduling alarms via AlarmManager; alarms are set via system Clock app
            }
        }
    }
    
    fun toggleSyncSchool(enabled: Boolean) {
        syncSchoolEnabled.value = enabled
        prefs.edit().putBoolean("syncSchoolEnabled", enabled).apply()
    }

    fun toggleSyncDeviceCalendar(enabled: Boolean) {
        syncDeviceCalendarEnabled.value = enabled
        prefs.edit().putBoolean("syncDeviceCalendarEnabled", enabled).apply()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun syncSchedule() {
        if (!syncSchoolEnabled.value && !syncDeviceCalendarEnabled.value) {
            syncState.value = SyncState.Error("Vui lòng bật ít nhất 1 nguồn đồng bộ")
            return
        }

        syncState.value = SyncState.Loading
        
        viewModelScope.launch {
            val activePlugin = repository.getInstalledPlugins().firstOrNull()

            // Sync school schedule
            if (syncSchoolEnabled.value) {
                if (activePlugin == null) {
                    syncState.value = SyncState.NoPlugin
                    return@launch
                }
                val result = repository.fetchSchedule(activePlugin, syncWeeks.value)
                if (result is SyncState.Error) {
                    syncState.value = result
                    return@launch
                }
                if (result is SyncState.Success) {
                    repository.saveScheduleItems(result.items)
                    eventCount.value = result.items.size
                    // Do not schedule alarms here; user sets alarms via Clock app when saving
                    if (result is SyncState.Success) {
                        repository.saveScheduleItems(result.items)
                        eventCount.value = result.items.size
                    }
                }
            }

            // Device calendar: only read, don't write
            // Reading device calendar events is handled by CalendarVM.loadDeviceCalendarEvents()
            // This sync only fetches and saves school schedule and alarms

            syncState.value = SyncState.Success(repository.getSavedScheduleItems())
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

    /**
     * Refresh the cached device calendar event count. Call this after READ_CALENDAR permission is granted.
     */
    fun refreshDeviceCount(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val count = try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    val startMs = java.time.LocalDate.now().minusMonths(1)
                        .atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val endMs = java.time.LocalDate.now().plusMonths(2)
                        .atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

                    val calendars = DeviceCalendarClient.getAvailableCalendars(context)
                    var c = 0
                    for (cal in calendars) {
                        try {
                            val items = DeviceCalendarClient.importFromDeviceCalendar(context, cal.id, startMs, endMs)
                            c += items.size
                        } catch (_: Exception) { }
                    }
                    c
                } else {
                    0
                }
            } catch (e: Exception) {
                0
            }

            withContext(Dispatchers.Main) {
                deviceCount.value = count
            }
        }
    }
}