package locapin.admin.auth

import io.ktor.server.application.call
import io.ktor.server.response.respondRedirect
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import io.ktor.server.routing.Route
import io.ktor.server.routing.intercept
import io.ktor.util.pipeline.PipelinePhase

fun Route.requireAdminSession() {
    intercept(PipelinePhase("AdminAuth")) {
        val session = call.sessions.get<AdminSession>()
        if (session == null) {
            call.respondRedirect("/admin/login?error=Please+log+in")
            finish()
            return@intercept
        }
    }
}
