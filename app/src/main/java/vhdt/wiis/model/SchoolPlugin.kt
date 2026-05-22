package vhdt.wiis.model
import kotlinx.serialization.Serializable

@Serializable
data class SchoolPlugin(
    val id: String,
    val schoolName: String,
    val loginUrl: String,
    val usernameSelector: String,
    val passwordSelector: String,
    val captchaSelector: String?,
    val tableScheduleSelector: String
)