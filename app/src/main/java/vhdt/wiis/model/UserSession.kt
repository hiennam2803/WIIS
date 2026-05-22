package vhdt.wiis.model

data class UserSession(
    val isSynced: Boolean = false,
    val currentSchoolId: String? = null,
    val lastSyncTime: Long = 0L,
    val encryptedUsername: String? = null,
    val encryptedPassword: String? = null
)