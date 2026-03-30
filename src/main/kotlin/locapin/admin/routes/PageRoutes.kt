package locapin.admin.routes

import io.ktor.server.application.call
import io.ktor.server.response.respondFile
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import locapin.admin.auth.AdminSession
import java.io.File

class PageRoutes(private val resourcesRoot: File = File("src/main/resources/static/html")) {
    fun register(route: Route) {
        route.get("/admin/login") {
            if (call.sessions.get<AdminSession>() != null) call.respondRedirect("/admin/dashboard")
            else call.respondFile(File(resourcesRoot, "login.html"))
        }
        secure(route, "/admin/dashboard", "dashboard.html")
        secure(route, "/admin/cities", "cities.html")
        secure(route, "/admin/cities/new", "city-form.html")
        secure(route, "/admin/cities/{id}/edit", "city-form.html")
        secure(route, "/admin/areas", "areas.html")
        secure(route, "/admin/areas/new", "area-form.html")
        secure(route, "/admin/areas/{id}/edit", "area-form.html")
        secure(route, "/admin/attractions", "attractions.html")
        secure(route, "/admin/attractions/new", "attraction-form.html")
        secure(route, "/admin/attractions/{id}", "attraction-detail.html")
        secure(route, "/admin/attractions/{id}/edit", "attraction-form.html")
        secure(route, "/admin/photos", "photos.html")
        secure(route, "/admin/plans", "plans.html")
    }

    private fun secure(route: Route, path: String, fileName: String) {
        route.get(path) {
            if (call.sessions.get<AdminSession>() == null) call.respondRedirect("/admin/login")
            else call.respondFile(File(resourcesRoot, fileName))
        }
    }
}
