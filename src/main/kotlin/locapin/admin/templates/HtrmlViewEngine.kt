package locapin.admin.templates

import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respondText
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import locapin.admin.auth.AdminSession

object HtmlViewEngine {
    data class RawHtml(val value: String)

    fun escape(input: String): String = buildString {
        input.forEach {
            append(
                when (it) {
                    '&' -> "&amp;"
                    '<' -> "&lt;"
                    '>' -> "&gt;"
                    '"' -> "&quot;"
                    '\'' -> "&#39;"
                    else -> it
                }
            )
        }
    }

    fun render(templatePath: String, model: Map<String, Any?>, useLayout: Boolean = true): String {
        val pageHtml = renderFile(templatePath, model)
        if (!useLayout) return pageHtml
        val layoutModel = model + mapOf("content" to RawHtml(pageHtml))
        return renderFile("layouts/base.html", layoutModel)
    }

    private fun renderFile(path: String, model: Map<String, Any?>): String {
        val raw = readResource("templates/$path")
        val included = includePartials(raw, model)
        return replacePlaceholders(included, model)
    }

    private fun includePartials(content: String, model: Map<String, Any?>): String {
        val regex = Regex("\\{\\{>\\s*([^}\\s]+)\\s*}}")
        var result = content
        regex.findAll(content).forEach { match ->
            val includePath = match.groupValues[1]
            val includeContent = renderFile(includePath, model)
            result = result.replace(match.value, includeContent)
        }
        return result
    }

    private fun replacePlaceholders(content: String, model: Map<String, Any?>): String {
        val regex = Regex("\\{\\{\\s*([a-zA-Z0-9_.-]+)\\s*}}")
        return regex.replace(content) { m ->
            val key = m.groupValues[1]
            val value = model[key] ?: ""
            when (value) {
                is RawHtml -> value.value
                else -> escape(value.toString())
            }
        }
    }

    private fun readResource(path: String): String {
        val stream = this::class.java.classLoader.getResourceAsStream(path)
            ?: error("Template not found: $path")
        return stream.bufferedReader().use { it.readText() }
    }
}

suspend fun ApplicationCall.renderTemplate(
    templatePath: String,
    model: Map<String, Any?> = emptyMap(),
    useLayout: Boolean = true
) {
    val admin = sessions.get<AdminSession>()
    val activeNav = model["activeNav"]?.toString().orEmpty()
    val base = mapOf(
        "pageTitle" to (model["pageTitle"]?.toString() ?: "LocaPin Admin"),
        "adminName" to (admin?.email ?: ""),
        "dashboardActiveClass" to if (activeNav == "dashboard") "active" else "",
        "citiesActiveClass" to if (activeNav == "cities") "active" else "",
        "areasActiveClass" to if (activeNav == "areas") "active" else "",
        "attractionsActiveClass" to if (activeNav == "attractions") "active" else "",
        "plansActiveClass" to if (activeNav == "plans") "active" else ""
    )
    val html = HtmlViewEngine.render(templatePath, base + model, useLayout)
    respondText(html, io.ktor.http.ContentType.Text.Html)
}
