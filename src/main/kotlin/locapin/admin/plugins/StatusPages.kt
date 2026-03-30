package locapin.admin.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.response.respondFile
import java.io.File

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<BadRequestException> { call, cause -> call.respond(mapOf("success" to false, "message" to cause.message)) }
        exception<Throwable> { call, _ -> call.respondFile(File("src/main/resources/static/html/500.html")) }
        status(io.ktor.http.HttpStatusCode.NotFound) { call, _ -> call.respondFile(File("src/main/resources/static/html/404.html")) }
    }
}
