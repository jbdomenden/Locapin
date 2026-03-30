package locapin.admin.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.sessions.SessionTransportTransformerMessageAuthentication
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.cookie
import locapin.admin.auth.AdminSession
import locapin.admin.config.AppConfig

fun Application.configureSecurity(config: AppConfig) {
    install(Sessions) {
        cookie<AdminSession>("locapin_admin_session") {
            cookie.path = "/"
            cookie.httpOnly = true
            cookie.maxAgeInSeconds = 86400
            transform(SessionTransportTransformerMessageAuthentication(config.sessionSecret.toByteArray()))
        }
    }
}
