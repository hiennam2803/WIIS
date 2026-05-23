package vhdt.wiis.app.setting

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.compose.runtime.mutableStateOf
import vhdt.wiis.model.AppSetting

class SettingVM(application: Application) : AndroidViewModel(application) {
    
    private val prefs = getApplication<Application>()
        .getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    
    val settings = mutableStateOf(AppSetting())
    
    init {
        settings.value = AppSetting(
            alarmEnabled = prefs.getBoolean("alarmEnabled", true),
            notifyMinutesBefore = prefs.getInt("notifyMinutesBefore", 60),
            snoozeMinutes = prefs.getInt("snoozeMinutes", 5),
            notificationEnabled = prefs.getBoolean("notificationEnabled", true),
            quietHoursEnabled = prefs.getBoolean("quietHoursEnabled", true),
            quietHoursStart = prefs.getString("quietHoursStart", "22:00") ?: "22:00",
            quietHoursEnd = prefs.getString("quietHoursEnd", "07:00") ?: "07:00",
            vibrationEnabled = prefs.getBoolean("vibrationEnabled", false)
        )
    }
    
    fun toggleAlarm(enabled: Boolean) {
        settings.value = settings.value.copy(alarmEnabled = enabled)
        prefs.edit().putBoolean("alarmEnabled", enabled).apply()
    }
    
    fun setNotifyMinutesBefore(minutes: Int) {
        settings.value = settings.value.copy(notifyMinutesBefore = minutes)
        prefs.edit().putInt("notifyMinutesBefore", minutes).apply()
    }
    
    fun setSnoozeMinutes(minutes: Int) {
        settings.value = settings.value.copy(snoozeMinutes = minutes)
        prefs.edit().putInt("snoozeMinutes", minutes).apply()
    }
    
    fun toggleNotification(enabled: Boolean) {
        settings.value = settings.value.copy(notificationEnabled = enabled)
        prefs.edit().putBoolean("notificationEnabled", enabled).apply()
    }
    
    fun toggleQuietHours(enabled: Boolean) {
        settings.value = settings.value.copy(quietHoursEnabled = enabled)
        prefs.edit().putBoolean("quietHoursEnabled", enabled).apply()
    }
    
    fun setQuietHoursStart(time: String) {
        settings.value = settings.value.copy(quietHoursStart = time)
        prefs.edit().putString("quietHoursStart", time).apply()
    }
    
    fun setQuietHoursEnd(time: String) {
        settings.value = settings.value.copy(quietHoursEnd = time)
        prefs.edit().putString("quietHoursEnd", time).apply()
    }
    
    fun toggleVibration(enabled: Boolean) {
        settings.value = settings.value.copy(vibrationEnabled = enabled)
        prefs.edit().putBoolean("vibrationEnabled", enabled).apply()
    }
    
    fun deleteAllData() {
        prefs.edit().clear().apply()
        settings.value = AppSetting()
    }
}