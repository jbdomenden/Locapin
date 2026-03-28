package locapin.admin.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import locapin.admin.templates.renderPage
import kotlinx.html.h2

fun Application.configureStatusPages() {
    install(StatusPages) {
        status(HttpStatusCode.Forbidden) { call, _ -> call.renderPage("403", null) { h2 { +"403 Forbidden" } } }
        status(HttpStatusCode.NotFound) { call, _ -> call.renderPage("404", null) { h2 { +"404 Not Found" } } }
        exception<Throwable> { call, _ -> call.renderPage("500", null) { h2 { +"500 Server Error" } } }
    }
}
