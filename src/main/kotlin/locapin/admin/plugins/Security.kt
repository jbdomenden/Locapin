package locapin.admin.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.sessions.SessionTransportTransformerEncrypt
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.cookie
import locapin.admin.auth.AdminSession
import locapin.admin.config.AppConfig
import java.security.MessageDigest

fun Application.configureSecurity(config: AppConfig) {
    install(Sessions) {
        cookie<AdminSession>("locapin_admin_session") {
            cookie.path = "/"
            cookie.httpOnly = true
            val digest = MessageDigest.getInstance("SHA-256").digest(config.sessionSecret.toByteArray())
            transform(SessionTransportTransformerEncrypt(digest.copyOfRange(0, 16), digest.copyOfRange(16, 32)))
        }
    }
}
