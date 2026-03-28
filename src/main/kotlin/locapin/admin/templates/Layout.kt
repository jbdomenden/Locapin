package locapin.admin.templates

import io.ktor.server.application.ApplicationCall
import io.ktor.server.html.respondHtml
import kotlinx.html.*
import locapin.admin.auth.AdminSession

suspend fun ApplicationCall.renderPage(
    pageTitle: String,
    session: AdminSession?,
    content: FlowContent.() -> Unit
) {
    respondHtml {
        head {
            meta { charset = "utf-8" }
            meta { name = "viewport"; content = "width=device-width,initial-scale=1" }
            title { +pageTitle }
            link(rel = "stylesheet", href = "/static/css/admin.css")
            script(src = "/static/js/admin.js") {}
        }
        body {
            if (session == null) {
                main(classes = "auth-shell") {
                    div(classes = "auth-card") { content() }
                }
            } else {
                div(classes = "app-shell") {
                    aside(classes = "sidebar") {
                        img(src = "/static/images/locapin-logo.svg", alt = "LocaPin") { classes = setOf("brand-logo") }
                        nav {
                            ul {
                                listOf(
                                    "Dashboard" to "/admin/dashboard",
                                    "Cities" to "/admin/cities",
                                    "Areas" to "/admin/areas",
                                    "Attractions" to "/admin/attractions",
                                    "Plans" to "/admin/plans"
                                ).forEach { (label, href) -> li { a(href) { +label } } }
                            }
                        }
                    }
                    div(classes = "main") {
                        header(classes = "topbar") {
                            h1 { +pageTitle }
                            div { +session.fullName; +" | "; a("/admin/logout") { +"Logout" } }
                        }
                        main(classes = "content") { content() }
                    }
                }
            }
        }
    }
}
