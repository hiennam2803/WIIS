package vhdt.wiis.data.repository

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
    
    companion object {
        private const val SCHEDULE_ITEMS_KEY = "schedule_items"
        private const val JWT_KEY_PREFIX = "jwt_"
    }
    
    fun getInstalledPlugins(): List<SchoolPlugin> {
        return try {
            PluginLoader.loadBuiltinPlugins(context)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun saveJwtToken(schoolId: String, token: String) {
        encryptedPrefs.edit().putString("$JWT_KEY_PREFIX$schoolId", token).apply()
    }
    
    fun getJwtToken(schoolId: String): String? {
        return encryptedPrefs.getString("$JWT_KEY_PREFIX$schoolId", null)
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchCurrentWeekSchedule(plugin: SchoolPlugin): SyncState {
        // Check if plugin is valid
        if (plugin.id.isBlank()) {
            return SyncState.NoPlugin
        }
        
        // Check if JWT token exists
        val jwtToken = getJwtToken(plugin.id) ?: return SyncState.Error("Chưa đăng nhập")
        
        // Fetch schedule for current week
        return try {
            val currentDate = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val scheduleItems = apiClient.fetchWeekSchedule(plugin, jwtToken, currentDate)
            
            if (scheduleItems.isEmpty()) {
                SyncState.Error("Không có lịch cho tuần này")
            } else {
                saveScheduleItems(scheduleItems)
                SyncState.Success(scheduleItems)
            }
        } catch (e: Exception) {
            SyncState.Error("Lỗi khi tải lịch: ${e.message}")
        }
    }
    
    fun saveScheduleItems(items: List<ScheduleItem>) {
        val jsonString = json.encodeToString(items)
        regularPrefs.edit().putString(SCHEDULE_ITEMS_KEY, jsonString).apply()
    }
    
    fun getSavedScheduleItems(): List<ScheduleItem> {
        return try {
            val jsonString = regularPrefs.getString(SCHEDULE_ITEMS_KEY, null) ?: return emptyList()
            json.decodeFromString(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun clearAllData() {
        regularPrefs.edit().clear().apply()
        encryptedPrefs.edit().clear().apply()
    }
}
