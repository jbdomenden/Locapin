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
import locapin.admin.models.RecordStatus
import locapin.admin.services.AdminContentService
import locapin.admin.services.AuthService
import locapin.admin.templates.renderPage
import java.math.BigDecimal

class AdminController(
    private val authService: AuthService = AuthService(),
    private val contentService: AdminContentService = AdminContentService()
) {
    private val attempts = mutableMapOf<String, Int>()

    suspend fun loginPage(ctx: RoutingContext) = ctx.call.renderPage("Admin Login", null) {
        h2 { +"LocaPin Admin" }
        p { +"Manage tourism content for San Juan City." }
        form("/admin/login", FormMethod.post) {
            label { +"Email" }
            textInput(name = "email") { required = true }
            label { +"Password" }
            passwordInput(name = "password") { required = true }
            button { +"Sign in" }
        }
    }

    suspend fun login(ctx: RoutingContext) {
        val params = ctx.call.receiveParameters()
        val email = params["email"].orEmpty().trim().lowercase()
        val password = params["password"].orEmpty()

        if ((attempts[email] ?: 0) >= 5) {
            ctx.call.response.status(HttpStatusCode.TooManyRequests)
            ctx.call.renderPage("Too many attempts", null) { p { +"Too many login attempts. Please wait and try again." } }
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
        val session = ctx.call.sessions.get<AdminSession>()
        val stats = contentService.dashboardStats()
        ctx.call.renderPage("Dashboard", session) {
            div("cards") {
                listOf(
                    "Cities" to stats.totalCities,
                    "Areas" to stats.totalAreas,
                    "Attractions" to stats.totalAttractions,
                    "Photos" to stats.totalPhotos,
                    "Users" to stats.totalUsers,
                    "Premium Subs" to stats.totalPremiumSubscribers
                ).forEach { (k, v) -> div("card") { h3 { +k }; p { +v.toString() } } }
            }
            h3 { +"Latest Attractions" }
            ul { stats.latestAttractions.forEach { li { +"${it.name} (${it.areaName})" } } }
        }
    }

    suspend fun cities(ctx: RoutingContext) {
        val session = ctx.call.sessions.get<AdminSession>()
        val cities = contentService.listCities()
        ctx.call.renderPage("Cities", session) {
            a("/admin/cities/new", classes = "btn") { +"Add City" }
            table {
                tr { th { +"Name" }; th { +"Premium" }; th { +"Status" }; th { +"Actions" } }
                cities.forEach { c -> tr { td { +c.name }; td { +c.isPremium.toString() }; td { +c.status.name }; td { a("/admin/cities/${c.id}/edit") { +"Edit" } } } }
            }
        }
    }

    suspend fun cityForm(ctx: RoutingContext, id: Long?) {
        val session = ctx.call.sessions.get<AdminSession>()
        val city = id?.let { contentService.getCity(it) }
        ctx.call.renderPage(if (id == null) "Create City" else "Edit City", session) {
            form(if (id == null) "/admin/cities" else "/admin/cities/$id", FormMethod.post) {
                label { +"Name" }; textInput(name = "name") { value = city?.name ?: ""; required = true }
                label { +"Premium" }; checkBoxInput(name = "isPremium") { checked = city?.isPremium == true }
                label { +"Status" }; select { name = "status"; RecordStatus.entries.forEach { option { value = it.name; +it.name; if (city?.status == it || (city == null && it == RecordStatus.ACTIVE)) selected = true } } }
                button { +"Save" }
            }
        }
    }

    suspend fun saveCity(ctx: RoutingContext, id: Long?) {
        val p = ctx.call.receiveParameters()
        contentService.saveCity(id, p["name"].orEmpty(), p["isPremium"] == "on", RecordStatus.valueOf(p["status"].orEmpty()))
        ctx.call.respondRedirect("/admin/cities")
    }

    suspend fun areas(ctx: RoutingContext) {
        val session = ctx.call.sessions.get<AdminSession>()
        val cities = contentService.listCities()
        val cityId = ctx.call.request.queryParameters["cityId"]?.toLongOrNull()
        val rows = contentService.listAreas(cityId)
        ctx.call.renderPage("Areas", session) {
            a("/admin/areas/new", classes = "btn") { +"Add Area" }
            form("/admin/areas", FormMethod.get) { select { name = "cityId"; option { value = ""; +"All cities" }; cities.forEach { option { value = it.id.toString(); +it.name } } }; button { +"Filter" } }
            table { tr { th { +"City" }; th { +"Name" }; th { +"Center" }; th { +"Status" }; th { +"Actions" } }
                rows.forEach { a -> tr { td { +a.cityName }; td { +a.name }; td { +"${a.centerLatitude}, ${a.centerLongitude}" }; td { +a.status.name }; td { a("/admin/areas/${a.id}/edit") { +"Edit" } } } }
            }
        }
    }

    suspend fun areaForm(ctx: RoutingContext, id: Long?) {
        val session = ctx.call.sessions.get<AdminSession>()
        val area = id?.let { contentService.getArea(it) }
        val cities = contentService.listCities()
        ctx.call.renderPage(if (id == null) "Create Area" else "Edit Area", session) {
            form(if (id == null) "/admin/areas" else "/admin/areas/$id", FormMethod.post) {
                label { +"City" }; select { name = "cityId"; cities.forEach { c -> option { value = c.id.toString(); +c.name; if (area?.cityId == c.id) selected = true } } }
                label { +"Name" }; textInput(name = "name") { value = area?.name ?: ""; required = true }
                label { +"Latitude" }; numberInput(name = "lat") { value = area?.centerLatitude?.toString() ?: ""; step = "any" }
                label { +"Longitude" }; numberInput(name = "lng") { value = area?.centerLongitude?.toString() ?: ""; step = "any" }
                label { +"Boundary Data" }; textArea { name = "boundary"; +"" }
                label { +"Status" }; select { name = "status"; RecordStatus.entries.forEach { option { value = it.name; +it.name; if (area?.status == it || (area == null && it == RecordStatus.ACTIVE)) selected = true } } }
                button { +"Save" }
            }
        }
    }

    suspend fun saveArea(ctx: RoutingContext, id: Long?) {
        val p = ctx.call.receiveParameters()
        contentService.saveArea(id, p["cityId"]!!.toLong(), p["name"].orEmpty(), p["lat"]!!.toDouble(), p["lng"]!!.toDouble(), p["boundary"], RecordStatus.valueOf(p["status"].orEmpty()))
        ctx.call.respondRedirect("/admin/areas")
    }

    suspend fun plans(ctx: RoutingContext) {
        val session = ctx.call.sessions.get<AdminSession>()
        val plans = contentService.listPlans()
        ctx.call.renderPage("Subscription Plans", session) {
            a("/admin/plans/new", classes = "btn") { +"Add Plan" }
            table { tr { th { +"Name" }; th { +"Period" }; th { +"Price" }; th { +"Active" }; th { +"Actions" } }
                plans.forEach { p -> tr { td { +p.name }; td { +p.billingPeriod.name }; td { +p.price.toPlainString() }; td { +p.isActive.toString() }; td { a("/admin/plans/${p.id}/edit") { +"Edit" } } } }
            }
        }
    }

    suspend fun planForm(ctx: RoutingContext, id: Long?) {
        val session = ctx.call.sessions.get<AdminSession>()
        val plan = id?.let { contentService.getPlan(it) }
        ctx.call.renderPage(if (id == null) "Create Plan" else "Edit Plan", session) {
            form(if (id == null) "/admin/plans" else "/admin/plans/$id", FormMethod.post) {
                label { +"Name" }; textInput(name = "name") { value = plan?.name ?: ""; required = true }
                label { +"Description" }; textArea { name = "description"; +(plan?.description ?: "") }
                label { +"Price" }; numberInput(name = "price") { value = plan?.price?.toPlainString() ?: ""; step = "0.01" }
                label { +"Billing period" }; select { name = "period"; locapin.admin.models.BillingPeriod.entries.forEach { bp -> option { value = bp.name; +bp.name; if (plan?.billingPeriod == bp) selected = true } } }
                label { +"Active" }; checkBoxInput(name = "isActive") { checked = plan?.isActive != false }
                button { +"Save" }
            }
        }
    }

    suspend fun savePlan(ctx: RoutingContext, id: Long?) {
        val p = ctx.call.receiveParameters()
        contentService.savePlan(id, p["name"].orEmpty(), p["description"].orEmpty(), p["price"]?.toBigDecimalOrNull() ?: BigDecimal.ZERO, locapin.admin.models.BillingPeriod.valueOf(p["period"].orEmpty()), p["isActive"] == "on")
        ctx.call.respondRedirect("/admin/plans")
    }
}
