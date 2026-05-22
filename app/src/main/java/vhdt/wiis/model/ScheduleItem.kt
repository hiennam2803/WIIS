package vhdt.wiis.model

data class ScheduleItem(
    val id: String,
    val title: String,
    val location: String,
    val note: String = "",
    val startTime: Long,
    val endTime: Long,
    val type: ScheduleType
)

enum class ScheduleType {
    UNIVERSITY, // Lịch trường
    PERSONAL,   // Lịch cá nhân
    MANUAL      // Nhập tay
}