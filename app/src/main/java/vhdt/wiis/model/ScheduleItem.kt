package vhdt.wiis.model
import kotlinx.serialization.Serializable

@Serializable
data class ScheduleItem(
    val id: String,
    val title: String,
    val location: String,
    val note: String = "",
    val startTime: Long,
    val endTime: Long,
    val type: ScheduleType
)

@Serializable
enum class ScheduleType {
    UNIVERSITY, // Lịch trường
    PERSONAL,   // Lịch cá nhân
    MANUAL      // Nhập tay
}