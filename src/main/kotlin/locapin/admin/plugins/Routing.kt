package locapin.admin.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.http.content.staticFiles
import io.ktor.server.response.respond
import io.ktor.server.routing.intercept
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import locapin.admin.auth.AdminSession
import locapin.admin.config.AppConfig
import locapin.admin.controllers.AdminApiController
import locapin.admin.controllers.AuthController
import locapin.admin.routes.PageRoutes
import locapin.admin.services.AdminService
import locapin.admin.services.FileStorageService
import java.io.File

fun Application.configureRouting(config: AppConfig) {
    val adminService = AdminService()
    val storage = FileStorageService(config.fileUploadDir)

    routing {
        staticFiles("/static", File("src/main/resources/static"))
        staticFiles("/uploads", File(config.fileUploadDir))

        AuthController().routes(this)
        PageRoutes().register(this)

        route("/admin/api") {
            intercept(io.ktor.util.pipeline.PipelinePhase("ApiAdminAuth")) {
                if (call.sessions.get<AdminSession>() == null) {
                    call.respond(io.ktor.http.HttpStatusCode.Unauthorized, mapOf("success" to false, "message" to "Unauthenticated"))
                    finish()
                }
            }
            AdminApiController(adminService, storage).routes(this)
        }
    }
}
