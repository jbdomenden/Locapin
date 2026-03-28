package locapin.admin.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import locapin.admin.templates.renderView

fun Application.configureStatusPages() {
    install(StatusPages) {
        status(HttpStatusCode.Forbidden) { call, _ -> call.renderView("errors/403.html", mapOf("pageTitle" to "403 Forbidden")) }
        status(HttpStatusCode.NotFound) { call, _ -> call.renderView("errors/404.html", mapOf("pageTitle" to "404 Not Found")) }
        exception<Throwable> { call, _ -> call.renderView("errors/500.html", mapOf("pageTitle" to "500 Server Error")) }
    }
}
