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
    val captchaSelector: String?,
    val tableScheduleSelector: String,
    val jsonMapping: JsonMapping
)

@Serializable
data class JsonMapping(
    val title: String,
    val location: String,
    val startTime: String,
    val endTime: String,
    val date: String
)