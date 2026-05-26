package vhdt.wiis.data.repository

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import vhdt.wiis.data.local.PluginLoader
import vhdt.wiis.data.remote.ScheduleApiClient
import vhdt.wiis.model.SchoolPlugin
import vhdt.wiis.model.ScheduleItem
import vhdt.wiis.model.SyncState
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class ScheduleRepository(
    private val context: Context,
    private val apiClient: ScheduleApiClient,
    private val pluginLoader: PluginLoader
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "jwt_tokens",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    private val regularPrefs = context.getSharedPreferences("schedule_data", Context.MODE_PRIVATE)
    private var cachedPlugins: List<SchoolPlugin>? = null
    private var cachedScheduleItems: List<ScheduleItem>? = null
    
    private val _scheduleItems = MutableStateFlow<List<ScheduleItem>>(emptyList())
    val scheduleItems: StateFlow<List<ScheduleItem>> = _scheduleItems.asStateFlow()
    
    companion object {
        private const val SCHEDULE_ITEMS_KEY = "schedule_items"
        private const val JWT_KEY_PREFIX = "jwt_"
    }
    
    fun getInstalledPlugins(): List<SchoolPlugin> {
        if (cachedPlugins == null) {
            cachedPlugins = try {
                PluginLoader.loadBuiltinPlugins(context)
            } catch (e: Exception) {
                emptyList()
            }
        }
        return cachedPlugins ?: emptyList()
    }
    
    fun addPlugin(plugin: SchoolPlugin) {
        cachedPlugins = (cachedPlugins ?: emptyList()) + plugin
    }
    
    fun getSavedScheduleItems(): List<ScheduleItem> {
        if (cachedScheduleItems == null) {
            cachedScheduleItems = try {
                val jsonString = regularPrefs.getString(SCHEDULE_ITEMS_KEY, null) ?: return emptyList()
                json.decodeFromString(jsonString)
            } catch (e: Exception) {
                emptyList()
            }
        }
        _scheduleItems.value = cachedScheduleItems ?: emptyList()
        return cachedScheduleItems ?: emptyList()
    }
    
    fun clearCache() {
        cachedPlugins = null
        cachedScheduleItems = null
    }
    
    fun saveJwtToken(schoolId: String, token: String) {
        encryptedPrefs.edit().putString("$JWT_KEY_PREFIX$schoolId", token).apply()
    }
    
    fun getJwtToken(schoolId: String): String? {
        return encryptedPrefs.getString("$JWT_KEY_PREFIX$schoolId", null)
    }
    
    fun clearJwtToken(schoolId: String) {
        encryptedPrefs.edit().remove("$JWT_KEY_PREFIX$schoolId").apply()
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchSchedule(plugin: SchoolPlugin, weeks: Int = 4): SyncState {
        // Check if plugin is valid
        if (plugin.id.isBlank()) {
            return SyncState.NoPlugin
        }
        
        // Check if JWT token exists
        val jwtToken = getJwtToken(plugin.id) ?: return SyncState.Error("AUTH_REQUIRED")
        
        // Fetch schedule for multiple weeks
        return try {
            val allItems = mutableListOf<ScheduleItem>()
            val today = LocalDate.now()

            repeat(weeks) { weekIndex ->
                val dateStr = today.plusWeeks(weekIndex.toLong())
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                val items = apiClient.fetchWeekSchedule(plugin, jwtToken, dateStr)
                if (items == null) {
                    // 401 Unauthorized — clear saved JWT and signal re-login
                    encryptedPrefs.edit().remove("$JWT_KEY_PREFIX${plugin.id}").apply()
                    return SyncState.Error("AUTH_REQUIRED")
                }
                allItems.addAll(items)
            }

            val uniqueItems = allItems.distinctBy { it.id }

            if (uniqueItems.isEmpty()) {
                SyncState.Error("Không có lịch")
            } else {
                saveScheduleItems(uniqueItems)
                SyncState.Success(uniqueItems)
            }
        } catch (e: Exception) {
            SyncState.Error("Lỗi: ${e.message}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchCurrentWeekSchedule(plugin: SchoolPlugin): SyncState {
        return fetchSchedule(plugin, 1)
    }
    
    fun saveScheduleItems(items: List<ScheduleItem>) {
        val jsonString = json.encodeToString(items)
        regularPrefs.edit().putString(SCHEDULE_ITEMS_KEY, jsonString).apply()
        cachedScheduleItems = items
        _scheduleItems.value = items
    }
    
    fun clearAllData() {
        regularPrefs.edit().clear().apply()
        encryptedPrefs.edit().clear().apply()
        cachedPlugins = null
        cachedScheduleItems = null
    }
}
