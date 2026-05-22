package vhdt.wiis.model

data class AppSetting(
    // Báo thức
    val alarmEnabled: Boolean = true,
    val notifyMinutesBefore: Int = 60,
    val snoozeMinutes: Int = 5,

    // Thông báo
    val notificationEnabled: Boolean = true,
    val quietHoursEnabled: Boolean = true,
    val quietHoursStart: String = "22:00",
    val quietHoursEnd: String = "07:00",
    val vibrationEnabled: Boolean = false
)
