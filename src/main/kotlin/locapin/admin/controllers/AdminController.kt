package locapin.admin.controllers

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.RoutingContext
import io.ktor.server.sessions.clear
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import locapin.admin.auth.AdminSession
import locapin.admin.models.BillingPeriod
import locapin.admin.models.RecordStatus
import locapin.admin.services.AdminContentService
import locapin.admin.services.AuthService
import locapin.admin.templates.renderView
import java.math.BigDecimal

class AdminController(
    private val authService: AuthService = AuthService(),
    private val contentService: AdminContentService = AdminContentService()
) {
    private val attempts = mutableMapOf<String, Int>()

    suspend fun loginPage(ctx: RoutingContext) {
        ctx.call.renderView("admin/login.html", mapOf("pageTitle" to "Admin Login"))
    }

    suspend fun login(ctx: RoutingContext) {
        val params = ctx.call.receiveParameters()
        val email = params["email"].orEmpty().trim().lowercase()
        val password = params["password"].orEmpty()

        if ((attempts[email] ?: 0) >= 5) {
            ctx.call.response.status(HttpStatusCode.TooManyRequests)
            ctx.call.renderView("admin/login.html", mapOf("pageTitle" to "Too many attempts", "error" to "Too many login attempts. Please wait and try again."))
            return
        }

        val admin = authService.authenticate(email, password)
        if (admin == null) {
            attempts[email] = (attempts[email] ?: 0) + 1
            ctx.call.respondRedirect("/admin/login?error=Invalid+credentials")
            return
        }

        attempts.remove(email)
        ctx.call.sessions.set(AdminSession(admin.id, admin.fullName, admin.email, admin.role))
        ctx.call.respondRedirect("/admin/dashboard")
    }

    suspend fun logout(ctx: RoutingContext) {
        ctx.call.sessions.clear<AdminSession>()
        ctx.call.respondRedirect("/admin/login")
    }

    suspend fun dashboard(ctx: RoutingContext) {
        val stats = contentService.dashboardStats()
        ctx.call.renderView("admin/dashboard.html", mapOf("pageTitle" to "Dashboard", "activeNav" to "dashboard", "stats" to stats))
    }

    suspend fun cities(ctx: RoutingContext) {
        val cities = contentService.listCities()
        ctx.call.renderView("admin/cities-list.html", mapOf("pageTitle" to "Cities", "activeNav" to "cities", "cities" to cities))
    }

    suspend fun cityForm(ctx: RoutingContext, id: Long?) {
        val city = id?.let { contentService.getCity(it) }
        ctx.call.renderView(
            "admin/city-form.html",
            mapOf("pageTitle" to if (id == null) "Create City" else "Edit City", "activeNav" to "cities", "city" to city, "statuses" to RecordStatus.entries)
        )
    }

    suspend fun saveCity(ctx: RoutingContext, id: Long?) {
        val p = ctx.call.receiveParameters()
        contentService.saveCity(id, p["name"].orEmpty(), p["isPremium"] == "on", RecordStatus.valueOf(p["status"].orEmpty()))
        ctx.call.respondRedirect("/admin/cities")
    }

    suspend fun areas(ctx: RoutingContext) {
        val cities = contentService.listCities()
        val cityId = ctx.call.request.queryParameters["cityId"]?.toLongOrNull()
        val rows = contentService.listAreas(cityId)
        ctx.call.renderView("admin/areas-list.html", mapOf("pageTitle" to "Areas", "activeNav" to "areas", "areas" to rows, "cities" to cities, "selectedCityId" to cityId))
    }

    suspend fun areaForm(ctx: RoutingContext, id: Long?) {
        val area = id?.let { contentService.getArea(it) }
        val cities = contentService.listCities()
        ctx.call.renderView(
            "admin/area-form.html",
            mapOf("pageTitle" to if (id == null) "Create Area" else "Edit Area", "activeNav" to "areas", "area" to area, "cities" to cities, "statuses" to RecordStatus.entries)
        )
    }

    suspend fun saveArea(ctx: RoutingContext, id: Long?) {
        val p = ctx.call.receiveParameters()
        contentService.saveArea(id, p["cityId"]!!.toLong(), p["name"].orEmpty(), p["lat"]!!.toDouble(), p["lng"]!!.toDouble(), p["boundary"], RecordStatus.valueOf(p["status"].orEmpty()))
        ctx.call.respondRedirect("/admin/areas")
    }

    suspend fun plans(ctx: RoutingContext) {
        val plans = contentService.listPlans()
        ctx.call.renderView("admin/plans-list.html", mapOf("pageTitle" to "Subscription Plans", "activeNav" to "plans", "plans" to plans))
    }

    suspend fun planForm(ctx: RoutingContext, id: Long?) {
        val plan = id?.let { contentService.getPlan(it) }
        ctx.call.renderView(
            "admin/plan-form.html",
            mapOf("pageTitle" to if (id == null) "Create Plan" else "Edit Plan", "activeNav" to "plans", "plan" to plan, "periods" to BillingPeriod.entries)
        )
    }

    suspend fun savePlan(ctx: RoutingContext, id: Long?) {
        val p = ctx.call.receiveParameters()
        contentService.savePlan(id, p["name"].orEmpty(), p["description"].orEmpty(), p["price"]?.toBigDecimalOrNull() ?: BigDecimal.ZERO, BillingPeriod.valueOf(p["period"].orEmpty()), p["isActive"] == "on")
        ctx.call.respondRedirect("/admin/plans")
    }
}
