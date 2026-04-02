package locapin.admin.controllers

import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import locapin.admin.models.*
import locapin.admin.services.AdminService
import locapin.admin.services.AuthorizationService
import locapin.admin.services.FileStorageService

class AdminApiController(
    private val service: AdminService,
    private val fileStorageService: FileStorageService,
    private val authorizationService: AuthorizationService = AuthorizationService()
) {
    fun routes(route: Route) {
        route.get("/admin/api/dashboard/stats") {
            if (!authorizationService.requireRead(call, ModuleKey.DASHBOARD)) return@get
            call.respond(ApiResponse(true, "Dashboard loaded", service.dashboard()))
        }

        route.get("/admin/api/cities") { if (!authorizationService.requireRead(call, ModuleKey.CITIES)) return@get; call.respond(ApiResponse(true, "Cities loaded", service.listCities())) }
        route.get("/admin/api/reference/ph-cities") {
            if (!authorizationService.requireRead(call, ModuleKey.CITIES)) return@get
            call.respond(ApiResponse(true, "City suggestions loaded", service.suggestPhilippineCities(call.request.queryParameters["q"])))
        }
        route.post("/admin/api/cities") { if (!authorizationService.requireCreate(call, ModuleKey.CITIES)) return@post; val req = call.receive<CityRequest>(); call.respond(HttpStatusCode.Created, ApiResponse(true, "City created", mapOf("id" to service.createCity(req)))) }
        route.get("/admin/api/cities/{id}") { if (!authorizationService.requireRead(call, ModuleKey.CITIES)) return@get; call.respond(ApiResponse(true, "City loaded", service.getCity(call.parameters["id"]!!.toLong()))) }
        route.put("/admin/api/cities/{id}") { if (!authorizationService.requireUpdate(call, ModuleKey.CITIES)) return@put; service.updateCity(call.parameters["id"]!!.toLong(), call.receive()); call.respond(ApiResponse<Unit>(true, "City updated")) }
        route.patch("/admin/api/cities/{id}/status") { if (!authorizationService.requireUpdate(call, ModuleKey.CITIES)) return@patch; service.updateCityStatus(call.parameters["id"]!!.toLong(), call.receive<StatusRequest>().status); call.respond(ApiResponse<Unit>(true, "City status updated")) }
        route.patch("/admin/api/cities/{id}/premium") { if (!authorizationService.requireUpdate(call, ModuleKey.CITIES)) return@patch; service.updateCityPremium(call.parameters["id"]!!.toLong(), call.receive<PremiumRequest>().isPremium); call.respond(ApiResponse<Unit>(true, "City premium updated")) }

        route.get("/admin/api/areas") { if (!authorizationService.requireRead(call, ModuleKey.AREAS)) return@get; call.respond(ApiResponse(true, "Areas loaded", service.listAreas(call.request.queryParameters["cityId"]?.toLongOrNull()))) }
        route.post("/admin/api/areas") { if (!authorizationService.requireCreate(call, ModuleKey.AREAS)) return@post; call.respond(HttpStatusCode.Created, ApiResponse(true, "Area created", mapOf("id" to service.createArea(call.receive())))) }
        route.get("/admin/api/areas/{id}") { if (!authorizationService.requireRead(call, ModuleKey.AREAS)) return@get; call.respond(ApiResponse(true, "Area loaded", service.getArea(call.parameters["id"]!!.toLong()))) }
        route.put("/admin/api/areas/{id}") { if (!authorizationService.requireUpdate(call, ModuleKey.AREAS)) return@put; service.updateArea(call.parameters["id"]!!.toLong(), call.receive()); call.respond(ApiResponse<Unit>(true, "Area updated")) }
        route.patch("/admin/api/areas/{id}/status") { if (!authorizationService.requireUpdate(call, ModuleKey.AREAS)) return@patch; service.updateAreaStatus(call.parameters["id"]!!.toLong(), call.receive<StatusRequest>().status); call.respond(ApiResponse<Unit>(true, "Area status updated")) }
        route.get("/admin/api/areas/by-city/{cityId}") { if (!authorizationService.requireRead(call, ModuleKey.AREAS)) return@get; call.respond(ApiResponse(true, "Areas loaded", service.areasByCity(call.parameters["cityId"]!!.toLong()))) }

        route.get("/admin/api/attractions") { if (!authorizationService.requireRead(call, ModuleKey.ATTRACTIONS)) return@get; call.respond(ApiResponse(true, "Attractions loaded", service.listAttractions(call.request.queryParameters["cityId"]?.toLongOrNull(), call.request.queryParameters["areaId"]?.toLongOrNull(), call.request.queryParameters["q"]))) }
        route.post("/admin/api/attractions") { if (!authorizationService.requireCreate(call, ModuleKey.ATTRACTIONS)) return@post; call.respond(HttpStatusCode.Created, ApiResponse(true, "Attraction created", mapOf("id" to service.createAttraction(call.receive())))) }
        route.get("/admin/api/attractions/{id}") { if (!authorizationService.requireRead(call, ModuleKey.ATTRACTIONS)) return@get; call.respond(ApiResponse(true, "Attraction loaded", service.getAttraction(call.parameters["id"]!!.toLong()))) }
        route.put("/admin/api/attractions/{id}") { if (!authorizationService.requireUpdate(call, ModuleKey.ATTRACTIONS)) return@put; service.updateAttraction(call.parameters["id"]!!.toLong(), call.receive()); call.respond(ApiResponse<Unit>(true, "Attraction updated")) }
        route.patch("/admin/api/attractions/{id}/status") { if (!authorizationService.requireUpdate(call, ModuleKey.ATTRACTIONS)) return@patch; service.updateAttractionStatus(call.parameters["id"]!!.toLong(), call.receive<StatusRequest>().status); call.respond(ApiResponse<Unit>(true, "Attraction status updated")) }
        route.patch("/admin/api/attractions/{id}/featured") { if (!authorizationService.requireUpdate(call, ModuleKey.ATTRACTIONS)) return@patch; service.updateAttractionFeatured(call.parameters["id"]!!.toLong(), call.receive<FeaturedRequest>().isFeatured); call.respond(ApiResponse<Unit>(true, "Attraction featured updated")) }
        route.delete("/admin/api/attractions/{id}") { if (!authorizationService.requireDelete(call, ModuleKey.ATTRACTIONS)) return@delete; service.deleteAttraction(call.parameters["id"]!!.toLong()); call.respond(ApiResponse<Unit>(true, "Attraction deleted")) }

        route.get("/admin/api/attractions/{id}/photos") { if (!authorizationService.requireRead(call, ModuleKey.PHOTOS)) return@get; call.respond(ApiResponse(true, "Photos loaded", service.listPhotos(call.parameters["id"]!!.toLong()))) }
        route.post("/admin/api/attractions/{id}/photos") {
            if (!authorizationService.requireCreate(call, ModuleKey.PHOTOS)) return@post
            val attractionId = call.parameters["id"]!!.toLong()
            val multipart = call.receiveMultipart()
            val uploaded = mutableListOf<Map<String, Any>>()
            multipart.forEachPart { part ->
                if (part is PartData.FileItem) {
                    val path = fileStorageService.saveImage(part)
                    val id = service.addPhoto(attractionId, path, uploaded.size)
                    uploaded.add(mapOf("id" to id, "path" to path))
                }
                part.dispose()
            }
            call.respond(HttpStatusCode.Created, ApiResponse(true, "Photos uploaded", uploaded))
        }
        route.patch("/admin/api/photos/reorder") { if (!authorizationService.requireUpdate(call, ModuleKey.PHOTOS)) return@patch; service.reorderPhotos(call.receive<PhotoReorderRequest>().items); call.respond(ApiResponse<Unit>(true, "Photos reordered")) }
        route.delete("/admin/api/photos/{id}") { if (!authorizationService.requireDelete(call, ModuleKey.PHOTOS)) return@delete; service.deletePhoto(call.parameters["id"]!!.toLong()); call.respond(ApiResponse<Unit>(true, "Photo deleted")) }

        route.get("/admin/api/plans") { if (!authorizationService.requireRead(call, ModuleKey.PLANS)) return@get; call.respond(ApiResponse(true, "Plans loaded", service.listPlans())) }
        route.post("/admin/api/plans") { if (!authorizationService.requireCreate(call, ModuleKey.PLANS)) return@post; call.respond(HttpStatusCode.Created, ApiResponse(true, "Plan created", mapOf("id" to service.createPlan(call.receive())))) }
        route.get("/admin/api/plans/{id}") { if (!authorizationService.requireRead(call, ModuleKey.PLANS)) return@get; call.respond(ApiResponse(true, "Plan loaded", service.getPlan(call.parameters["id"]!!.toLong()))) }
        route.put("/admin/api/plans/{id}") { if (!authorizationService.requireUpdate(call, ModuleKey.PLANS)) return@put; service.updatePlan(call.parameters["id"]!!.toLong(), call.receive()); call.respond(ApiResponse<Unit>(true, "Plan updated")) }
        route.patch("/admin/api/plans/{id}/status") { if (!authorizationService.requireUpdate(call, ModuleKey.PLANS)) return@patch; service.updatePlanStatus(call.parameters["id"]!!.toLong(), call.receive<mapActive>().isActive); call.respond(ApiResponse<Unit>(true, "Plan status updated")) }

        route.get("/admin/api/users") {
            if (!authorizationService.requireRead(call, ModuleKey.USER_MANAGEMENT)) return@get
            val role = call.request.queryParameters["role"]?.let { AdminRole.valueOf(it) }
            val status = call.request.queryParameters["status"]?.let { AdminAccountStatus.valueOf(it) }
            call.respond(ApiResponse(true, "Users loaded", service.listAdminUsers(call.request.queryParameters["q"], role, status)))
        }
        route.post("/admin/api/users") {
            if (!authorizationService.requireCreate(call, ModuleKey.USER_MANAGEMENT)) return@post
            val actor = call.sessions.get<locapin.admin.auth.AdminSession>()!!.adminId
            call.respond(HttpStatusCode.Created, ApiResponse(true, "User created", mapOf("id" to service.createAdminUser(actor, call.receive()))))
        }
        route.get("/admin/api/users/{id}") { if (!authorizationService.requireRead(call, ModuleKey.USER_MANAGEMENT)) return@get; call.respond(ApiResponse(true, "User loaded", service.getAdminUser(call.parameters["id"]!!.toLong()))) }
        route.put("/admin/api/users/{id}") { if (!authorizationService.requireUpdate(call, ModuleKey.USER_MANAGEMENT)) return@put; service.updateAdminUser(call.parameters["id"]!!.toLong(), call.receive()); call.respond(ApiResponse<Unit>(true, "User updated")) }
        route.patch("/admin/api/users/{id}/status") { if (!authorizationService.requireUpdate(call, ModuleKey.USER_MANAGEMENT)) return@patch; service.updateAdminStatus(call.parameters["id"]!!.toLong(), call.receive<mapStatus>().status); call.respond(ApiResponse<Unit>(true, "User status updated")) }
        route.delete("/admin/api/users/{id}") { if (!authorizationService.requireDelete(call, ModuleKey.USER_MANAGEMENT)) return@delete; service.deleteAdminUser(call.parameters["id"]!!.toLong()); call.respond(ApiResponse<Unit>(true, "User deleted")) }
        route.post("/admin/api/users/{id}/reset-password") { if (!authorizationService.requireUpdate(call, ModuleKey.USER_MANAGEMENT)) return@post; service.resetAdminPassword(call.parameters["id"]!!.toLong(), call.receive()); call.respond(ApiResponse<Unit>(true, "Password reset")) }
        route.get("/admin/api/users/{id}/permissions") { if (!authorizationService.requireRead(call, ModuleKey.USER_MANAGEMENT)) return@get; call.respond(ApiResponse(true, "Permissions loaded", service.getAdminPermissions(call.parameters["id"]!!.toLong()))) }
        route.put("/admin/api/users/{id}/permissions") { if (!authorizationService.requireUpdate(call, ModuleKey.USER_MANAGEMENT)) return@put; service.setAdminPermissions(call.parameters["id"]!!.toLong(), call.receive()); call.respond(ApiResponse<Unit>(true, "Permissions updated")) }
    }

    @kotlinx.serialization.Serializable
    private data class mapActive(val isActive: Boolean)
    @kotlinx.serialization.Serializable
    private data class mapStatus(val status: AdminAccountStatus)
}
