package vhdt.wiis.model
import kotlinx.serialization.Serializable

@Serializable
data class ScheduleItem(
    val id: String,
    val title: String,
    val location: String,        // Phòng: H204 - CS3
    val note: String = "",       // Cơ sở: P.Trung Mỹ Tây
    val startTime: Long,
    val endTime: Long,
    val type: ScheduleType,
    // THÊM MỚI
    val classCode: String = "",  // 010112400804
    val fromPeriod: Int = 0,     // Tiết 1
    val toPeriod: Int = 0,       // Tiết 3
    val address: String = ""     // P.Trung Mỹ Tây, TP.HCM
)

@Serializable
enum class ScheduleType {
    UNIVERSITY, // Lịch trường
    PERSONAL,   // Lịch cá nhân
    MANUAL      // Nhập tay
}