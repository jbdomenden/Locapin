package locapin.admin.auth

import kotlinx.serialization.Serializable
import locapin.admin.models.AdminRole

@Serializable
data class AdminSession(
    val adminId: Long,
    val fullName: String,
    val email: String,
    val role: AdminRole
)
