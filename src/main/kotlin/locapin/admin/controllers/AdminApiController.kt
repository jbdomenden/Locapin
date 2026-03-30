package locapin.admin.controllers

import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import locapin.admin.models.*
import locapin.admin.services.AdminService
import locapin.admin.services.FileStorageService

class AdminApiController(
    private val service: AdminService,
    private val fileStorageService: FileStorageService
) {
    fun routes(route: Route) {
        route.get("/admin/api/dashboard/stats") { call.respond(ApiResponse(true, "Dashboard loaded", service.dashboard())) }

        route.get("/admin/api/cities") { call.respond(ApiResponse(true, "Cities loaded", service.listCities())) }
        route.post("/admin/api/cities") { val req = call.receive<CityRequest>(); call.respond(HttpStatusCode.Created, ApiResponse(true, "City created", mapOf("id" to service.createCity(req)))) }
        route.get("/admin/api/cities/{id}") { call.respond(ApiResponse(true, "City loaded", service.getCity(call.parameters["id"]!!.toLong()))) }
        route.put("/admin/api/cities/{id}") { service.updateCity(call.parameters["id"]!!.toLong(), call.receive()); call.respond(ApiResponse<Unit>(true, "City updated")) }
        route.patch("/admin/api/cities/{id}/status") { service.updateCityStatus(call.parameters["id"]!!.toLong(), call.receive<StatusRequest>().status); call.respond(ApiResponse<Unit>(true, "City status updated")) }
        route.patch("/admin/api/cities/{id}/premium") { service.updateCityPremium(call.parameters["id"]!!.toLong(), call.receive<PremiumRequest>().isPremium); call.respond(ApiResponse<Unit>(true, "City premium updated")) }

        route.get("/admin/api/areas") { call.respond(ApiResponse(true, "Areas loaded", service.listAreas(call.request.queryParameters["cityId"]?.toLongOrNull()))) }
        route.post("/admin/api/areas") { call.respond(HttpStatusCode.Created, ApiResponse(true, "Area created", mapOf("id" to service.createArea(call.receive())))) }
        route.get("/admin/api/areas/{id}") { call.respond(ApiResponse(true, "Area loaded", service.getArea(call.parameters["id"]!!.toLong()))) }
        route.put("/admin/api/areas/{id}") { service.updateArea(call.parameters["id"]!!.toLong(), call.receive()); call.respond(ApiResponse<Unit>(true, "Area updated")) }
        route.patch("/admin/api/areas/{id}/status") { service.updateAreaStatus(call.parameters["id"]!!.toLong(), call.receive<StatusRequest>().status); call.respond(ApiResponse<Unit>(true, "Area status updated")) }
        route.get("/admin/api/areas/by-city/{cityId}") { call.respond(ApiResponse(true, "Areas loaded", service.areasByCity(call.parameters["cityId"]!!.toLong()))) }

        route.get("/admin/api/attractions") { call.respond(ApiResponse(true, "Attractions loaded", service.listAttractions(call.request.queryParameters["cityId"]?.toLongOrNull(), call.request.queryParameters["areaId"]?.toLongOrNull(), call.request.queryParameters["q"]))) }
        route.post("/admin/api/attractions") { call.respond(HttpStatusCode.Created, ApiResponse(true, "Attraction created", mapOf("id" to service.createAttraction(call.receive())))) }
        route.get("/admin/api/attractions/{id}") { call.respond(ApiResponse(true, "Attraction loaded", service.getAttraction(call.parameters["id"]!!.toLong()))) }
        route.put("/admin/api/attractions/{id}") { service.updateAttraction(call.parameters["id"]!!.toLong(), call.receive()); call.respond(ApiResponse<Unit>(true, "Attraction updated")) }
        route.patch("/admin/api/attractions/{id}/status") { service.updateAttractionStatus(call.parameters["id"]!!.toLong(), call.receive<StatusRequest>().status); call.respond(ApiResponse<Unit>(true, "Attraction status updated")) }
        route.patch("/admin/api/attractions/{id}/featured") { service.updateAttractionFeatured(call.parameters["id"]!!.toLong(), call.receive<FeaturedRequest>().isFeatured); call.respond(ApiResponse<Unit>(true, "Attraction featured updated")) }
        route.delete("/admin/api/attractions/{id}") { service.deleteAttraction(call.parameters["id"]!!.toLong()); call.respond(ApiResponse<Unit>(true, "Attraction deleted")) }

        route.get("/admin/api/attractions/{id}/photos") { call.respond(ApiResponse(true, "Photos loaded", service.listPhotos(call.parameters["id"]!!.toLong()))) }
        route.post("/admin/api/attractions/{id}/photos") {
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
        route.patch("/admin/api/photos/reorder") { service.reorderPhotos(call.receive<PhotoReorderRequest>().items); call.respond(ApiResponse<Unit>(true, "Photos reordered")) }
        route.delete("/admin/api/photos/{id}") { service.deletePhoto(call.parameters["id"]!!.toLong()); call.respond(ApiResponse<Unit>(true, "Photo deleted")) }

        route.get("/admin/api/plans") { call.respond(ApiResponse(true, "Plans loaded", service.listPlans())) }
        route.post("/admin/api/plans") { call.respond(HttpStatusCode.Created, ApiResponse(true, "Plan created", mapOf("id" to service.createPlan(call.receive())))) }
        route.get("/admin/api/plans/{id}") { call.respond(ApiResponse(true, "Plan loaded", service.getPlan(call.parameters["id"]!!.toLong()))) }
        route.put("/admin/api/plans/{id}") { service.updatePlan(call.parameters["id"]!!.toLong(), call.receive()); call.respond(ApiResponse<Unit>(true, "Plan updated")) }
        route.patch("/admin/api/plans/{id}/status") { service.updatePlanStatus(call.parameters["id"]!!.toLong(), call.receive<mapActive>().isActive); call.respond(ApiResponse<Unit>(true, "Plan status updated")) }
    }

    @kotlinx.serialization.Serializable
    private data class mapActive(val isActive: Boolean)
}
