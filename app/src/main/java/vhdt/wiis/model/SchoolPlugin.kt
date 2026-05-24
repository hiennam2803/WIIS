package vhdt.wiis.model

import kotlinx.serialization.Serializable

@Serializable
data class SchoolPlugin(
    val id: String,
    val schoolName: String,
    val loginUrl: String,
    val scheduleEndpoint: String,
    val authType: String,
    val usernameSelector: String,
    val passwordSelector: String,
    val captchaSelector: String? = null,
    val tableScheduleSelector: String,
    val jsonMapping: JsonMapping,
    val responseBodyKey: String? = null,   // key bọc ngoài, ví dụ "body"
    val timeFormat: String = "HH:mm"       // "HH:mm" hoặc "tiet" (số tiết)
)

@Serializable
data class JsonMapping(
    val title: String,
    val location: String,
    val startTime: String,
    val endTime: String,
    val date: String,
    val note: String? = null,
    // THÊM MỚI
    val classCode: String? = null,   // "maLopHocPhan"
    val fromPeriod: String? = null,  // "tuTiet"
    val toPeriod: String? = null,    // "denTiet"
    val address: String? = null      // "tenPhong" đầy đủ
)