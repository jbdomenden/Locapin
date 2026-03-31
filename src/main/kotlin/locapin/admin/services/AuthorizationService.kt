package locapin.admin.services

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import locapin.admin.auth.AdminSession
import locapin.admin.models.AdminRole
import locapin.admin.models.ModuleKey
import locapin.admin.repositories.AdminRepository

class AuthorizationService(private val adminRepository: AdminRepository = AdminRepository()) {
    suspend fun requireRead(call: ApplicationCall, module: ModuleKey): Boolean = require(call, module) { it.canRead }
    suspend fun requireCreate(call: ApplicationCall, module: ModuleKey): Boolean = require(call, module) { it.canCreate }
    suspend fun requireUpdate(call: ApplicationCall, module: ModuleKey): Boolean = require(call, module) { it.canUpdate }
    suspend fun requireDelete(call: ApplicationCall, module: ModuleKey): Boolean = require(call, module) { it.canDelete }

    fun canRead(session: AdminSession?, module: ModuleKey): Boolean {
        if (session == null) return false
        if (session.role == AdminRole.SUPER_ADMIN) return true
        return adminRepository.getPermissions(session.adminId).any { it.moduleKey == module && it.canRead }
    }

    private suspend fun require(call: ApplicationCall, module: ModuleKey, check: (locapin.admin.repositories.PermissionRecord) -> Boolean): Boolean {
        val session = call.sessions.get<AdminSession>()
        if (session == null) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("success" to false, "message" to "Unauthenticated"))
            return false
        }
        if (session.role == AdminRole.SUPER_ADMIN) return true
        val has = adminRepository.getPermissions(session.adminId).any { it.moduleKey == module && check(it) }
        if (!has) {
            call.respond(HttpStatusCode.Forbidden, mapOf("success" to false, "message" to "Insufficient permission"))
            return false
        }
        return true
    }
}
