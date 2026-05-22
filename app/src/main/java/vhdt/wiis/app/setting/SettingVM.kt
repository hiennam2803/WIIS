package vhdt.wiis.app.setting

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf

data class AppSettings(
    val alarmEnabled: Boolean = true,
    val notifyMinutesBefore: Int = 60,
    val snoozeMinutes: Int = 5,
    val notificationEnabled: Boolean = true,
    val quietHoursEnabled: Boolean = true,
    val quietHoursStart: String = "22:00",
    val quietHoursEnd: String = "07:00",
    val vibrationEnabled: Boolean = true
)

class SettingVM : ViewModel() {
    val settings = mutableStateOf(AppSettings())
    
    fun toggleAlarm(enabled: Boolean) {
        settings.value = settings.value.copy(alarmEnabled = enabled)
    }
    
    fun setNotifyMinutesBefore(minutes: Int) {
        settings.value = settings.value.copy(notifyMinutesBefore = minutes)
    }
    
    fun setSnoozeMinutes(minutes: Int) {
        settings.value = settings.value.copy(snoozeMinutes = minutes)
    }
    
    fun toggleNotification(enabled: Boolean) {
        settings.value = settings.value.copy(notificationEnabled = enabled)
    }
    
    fun toggleQuietHours(enabled: Boolean) {
        settings.value = settings.value.copy(quietHoursEnabled = enabled)
    }
    
    fun setQuietHoursStart(time: String) {
        settings.value = settings.value.copy(quietHoursStart = time)
    }
    
    fun setQuietHoursEnd(time: String) {
        settings.value = settings.value.copy(quietHoursEnd = time)
    }
    
    fun toggleVibration(enabled: Boolean) {
        settings.value = settings.value.copy(vibrationEnabled = enabled)
    }
    
    fun deleteAllData() {
        // TODO: Implement delete all data logic
    }
}