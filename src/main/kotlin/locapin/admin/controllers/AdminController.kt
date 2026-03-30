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
import locapin.admin.templates.HtmlViewEngine
import locapin.admin.templates.HtmlViewEngine.RawHtml
import locapin.admin.templates.renderTemplate
import java.math.BigDecimal

class AdminController(
    private val authService: AuthService = AuthService(),
    private val contentService: AdminContentService = AdminContentService()
) {
    private val attempts = mutableMapOf<String, Int>()

    suspend fun loginPage(ctx: RoutingContext) {
        ctx.call.renderTemplate("admin/login.html", mapOf("pageTitle" to "Admin Login", "error" to ctx.call.request.queryParameters["error"].orEmpty()))
    }

    suspend fun login(ctx: RoutingContext) {
        val params = ctx.call.receiveParameters()
        val email = params["email"].orEmpty().trim().lowercase()
        val password = params["password"].orEmpty()

        if ((attempts[email] ?: 0) >= 5) {
            ctx.call.response.status(HttpStatusCode.TooManyRequests)
            ctx.call.renderTemplate("admin/login.html", mapOf("pageTitle" to "Too many attempts", "error" to "Too many login attempts. Please wait and try again."))
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
        val cards = RawHtml(
            listOf(
                "Cities" to stats.totalCities,
                "Areas" to stats.totalAreas,
                "Attractions" to stats.totalAttractions,
                "Photos" to stats.totalPhotos,
                "Users" to stats.totalUsers,
                "Premium Subs" to stats.totalPremiumSubscribers
            ).joinToString("") { (k, v) -> "<div class='card'><h3>${HtmlViewEngine.escape(k)}</h3><p>${v}</p></div>" }
        )
        val latest = RawHtml(stats.latestAttractions.joinToString("") { "<li>${HtmlViewEngine.escape(it.name)} (${HtmlViewEngine.escape(it.areaName)})</li>" })
        ctx.call.renderTemplate("admin/dashboard.html", mapOf("pageTitle" to "Dashboard", "activeNav" to "dashboard", "dashboardCards" to cards, "latestAttractions" to latest))
    }

    suspend fun cities(ctx: RoutingContext) {
        val cities = contentService.listCities()
        val rows = RawHtml(cities.joinToString("") {
            "<tr><td>${HtmlViewEngine.escape(it.name)}</td><td>${if (it.isPremium) "Yes" else "No"}</td><td>${it.status}</td><td><a href='/admin/cities/${it.id}/edit'>Edit</a></td></tr>"
        })
        ctx.call.renderTemplate("admin/cities-list.html", mapOf("pageTitle" to "Cities", "activeNav" to "cities", "cityRows" to rows))
    }

    suspend fun cityForm(ctx: RoutingContext, id: Long?) {
        val city = id?.let { contentService.getCity(it) }
        val statusOptions = RawHtml(RecordStatus.entries.joinToString("") {
            val selected = if ((city?.status ?: RecordStatus.ACTIVE) == it) "selected" else ""
            "<option value='$it' $selected>$it</option>"
        })
        ctx.call.renderTemplate("admin/city-form.html", mapOf(
            "pageTitle" to if (id == null) "Create City" else "Edit City",
            "activeNav" to "cities",
            "formAction" to if (id == null) "/admin/cities" else "/admin/cities/$id",
            "cityName" to (city?.name ?: ""),
            "cityPremiumChecked" to if (city?.isPremium == true) "checked" else "",
            "statusOptions" to statusOptions
        ))
    }

    suspend fun saveCity(ctx: RoutingContext, id: Long?) {
        val p = ctx.call.receiveParameters()
        contentService.saveCity(id, p["name"].orEmpty(), p["isPremium"] == "on", RecordStatus.valueOf(p["status"].orEmpty()))
        ctx.call.respondRedirect("/admin/cities")
    }

    suspend fun areas(ctx: RoutingContext) {
        val cities = contentService.listCities()
        val selected = ctx.call.request.queryParameters["cityId"]?.toLongOrNull()
        val rows = contentService.listAreas(selected)
        val cityOptions = RawHtml(cities.joinToString("") {
            val s = if (selected == it.id) "selected" else ""
            "<option value='${it.id}' $s>${HtmlViewEngine.escape(it.name)}</option>"
        })
        val areaRows = RawHtml(rows.joinToString("") {
            "<tr><td>${HtmlViewEngine.escape(it.cityName)}</td><td>${HtmlViewEngine.escape(it.name)}</td><td>${it.centerLatitude}, ${it.centerLongitude}</td><td>${it.status}</td><td><a href='/admin/areas/${it.id}/edit'>Edit</a></td></tr>"
        })
        ctx.call.renderTemplate("admin/areas-list.html", mapOf("pageTitle" to "Areas", "activeNav" to "areas", "cityOptions" to cityOptions, "areaRows" to areaRows))
    }

    suspend fun areaForm(ctx: RoutingContext, id: Long?) {
        val area = id?.let { contentService.getArea(it) }
        val cities = contentService.listCities()
        val cityOptions = RawHtml(cities.joinToString("") {
            val s = if (area?.cityId == it.id) "selected" else ""
            "<option value='${it.id}' $s>${HtmlViewEngine.escape(it.name)}</option>"
        })
        val statusOptions = RawHtml(RecordStatus.entries.joinToString("") {
            val selected = if ((area?.status ?: RecordStatus.ACTIVE) == it) "selected" else ""
            "<option value='$it' $selected>$it</option>"
        })
        ctx.call.renderTemplate("admin/area-form.html", mapOf(
            "pageTitle" to if (id == null) "Create Area" else "Edit Area",
            "activeNav" to "areas",
            "formAction" to if (id == null) "/admin/areas" else "/admin/areas/$id",
            "cityOptions" to cityOptions,
            "areaName" to (area?.name ?: ""),
            "areaLat" to (area?.centerLatitude?.toString() ?: ""),
            "areaLng" to (area?.centerLongitude?.toString() ?: ""),
            "statusOptions" to statusOptions
        ))
    }

    suspend fun saveArea(ctx: RoutingContext, id: Long?) {
        val p = ctx.call.receiveParameters()
        contentService.saveArea(id, p["cityId"]!!.toLong(), p["name"].orEmpty(), p["lat"]!!.toDouble(), p["lng"]!!.toDouble(), p["boundary"], RecordStatus.valueOf(p["status"].orEmpty()))
        ctx.call.respondRedirect("/admin/areas")
    }

    suspend fun plans(ctx: RoutingContext) {
        val rows = RawHtml(contentService.listPlans().joinToString("") {
            "<tr><td>${HtmlViewEngine.escape(it.name)}</td><td>${it.billingPeriod}</td><td>${it.price}</td><td>${if (it.isActive) "Yes" else "No"}</td><td><a href='/admin/plans/${it.id}/edit'>Edit</a></td></tr>"
        })
        ctx.call.renderTemplate("admin/plans-list.html", mapOf("pageTitle" to "Subscription Plans", "activeNav" to "plans", "planRows" to rows))
    }

    suspend fun planForm(ctx: RoutingContext, id: Long?) {
        val plan = id?.let { contentService.getPlan(it) }
        val periodOptions = RawHtml(BillingPeriod.entries.joinToString("") {
            val selected = if ((plan?.billingPeriod ?: BillingPeriod.MONTHLY) == it) "selected" else ""
            "<option value='$it' $selected>$it</option>"
        })
        ctx.call.renderTemplate("admin/plan-form.html", mapOf(
            "pageTitle" to if (id == null) "Create Plan" else "Edit Plan",
            "activeNav" to "plans",
            "formAction" to if (id == null) "/admin/plans" else "/admin/plans/$id",
            "planName" to (plan?.name ?: ""),
            "planDescription" to (plan?.description ?: ""),
            "planPrice" to (plan?.price?.toString() ?: ""),
            "periodOptions" to periodOptions,
            "planActiveChecked" to if (plan?.isActive != false) "checked" else ""
        ))
    }

    suspend fun savePlan(ctx: RoutingContext, id: Long?) {
        val p = ctx.call.receiveParameters()
        contentService.savePlan(id, p["name"].orEmpty(), p["description"].orEmpty(), p["price"]?.toBigDecimalOrNull() ?: BigDecimal.ZERO, BillingPeriod.valueOf(p["period"].orEmpty()), p["isActive"] == "on")
        ctx.call.respondRedirect("/admin/plans")
    }
}
