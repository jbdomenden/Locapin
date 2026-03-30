package locapin.admin.controllers

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.sessions.clear
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import locapin.admin.auth.AdminSession
import locapin.admin.models.ApiResponse
import locapin.admin.models.ChangePasswordRequest
import locapin.admin.models.LoginRequest
import locapin.admin.services.AuthService

class AuthController(private val authService: AuthService = AuthService()) {
    fun routes(route: Route) {
        route.post("/admin/auth/login") {
            val payload = call.receive<LoginRequest>()
            val admin = authService.login(payload.email, payload.password)
                ?: return@post call.respond(HttpStatusCode.Unauthorized, ApiResponse<Unit>(false, "Invalid credentials"))
            call.sessions.set(AdminSession(admin.id, admin.email, admin.role))
            call.respond(ApiResponse(true, "Login successful", admin))
        }
        route.post("/admin/auth/logout") {
            call.sessions.clear<AdminSession>()
            call.respond(ApiResponse<Unit>(true, "Logged out"))
        }
        route.get("/admin/auth/me") {
            val session = call.sessions.get<AdminSession>()
            if (session == null) call.respond(HttpStatusCode.Unauthorized, ApiResponse<Unit>(false, "Unauthenticated"))
            else call.respond(ApiResponse(true, "Authenticated", session))
        }

        route.post("/admin/auth/change-password") {
            val session = call.sessions.get<AdminSession>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized, ApiResponse<Unit>(false, "Unauthenticated"))
            val payload = call.receive<ChangePasswordRequest>()
            val error = authService.changePassword(
                adminId = session.adminId,
                currentPassword = payload.currentPassword,
                newPassword = payload.newPassword,
                confirmNewPassword = payload.confirmNewPassword
            )
            if (error != null) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(false, error))
            } else {
                call.respond(ApiResponse<Unit>(true, "Password changed successfully."))
            }
        }
    }
}
