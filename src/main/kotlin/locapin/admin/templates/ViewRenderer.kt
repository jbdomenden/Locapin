package locapin.admin.templates

import io.ktor.server.application.ApplicationCall
import io.ktor.server.freemarker.FreeMarkerContent
import io.ktor.server.response.respond
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import locapin.admin.auth.AdminSession

suspend fun ApplicationCall.renderView(template: String, model: Map<String, Any?> = emptyMap()) {
    val session = sessions.get<AdminSession>()
    val baseModel = mutableMapOf<String, Any?>(
        "currentAdmin" to session,
        "currentPath" to request.path(),
        "pageTitle" to "LocaPin Admin"
    )
    baseModel.putAll(model)
    respond(FreeMarkerContent(template, baseModel))
}
