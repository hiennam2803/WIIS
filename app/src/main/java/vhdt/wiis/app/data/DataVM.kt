package vhdt.wiis.app.data

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf

data class PluginInfo(
    val id: String,
    val schoolName: String,
    val schoolUrl: String,
    val version: String,
    val status: String
)

data class SyncInfo(
    val lastSyncTime: Long? = null,
    val isSyncing: Boolean = false,
    val error: String? = null
)

class DataVM : ViewModel() {
    val pluginCount = mutableStateOf(2)
    val eventCount = mutableStateOf(12)
    val alarmCount = mutableStateOf(5)
    
    val plugins = listOf(
        PluginInfo(
            id = "hcmut_v2",
            schoolName = "Đại học Bách Khoa",
            schoolUrl = "hcmut.edu.vn - v2",
            version = "2.0",
            status = "Tăng dụng"
        ),
        PluginInfo(
            id = "gym_aloha",
            schoolName = "Gym Aloha",
            schoolUrl = "nhập key local",
            version = "1.0",
            status = "Thỏ ỏng"
        )
    )
    
    val syncState = mutableStateOf(SyncInfo())
    
    fun syncSchedule() {
        syncState.value = SyncInfo(isSyncing = true)
        // TODO: Implement sync logic
    }
    
    fun syncGoogleCalendar() {
        syncState.value = SyncInfo(isSyncing = true)
        // TODO: Implement Google Calendar sync
    }
    
    fun importPluginJson() {
        // TODO: Implement JSON import
    }
}