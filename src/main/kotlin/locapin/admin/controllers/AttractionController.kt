package locapin.admin.controllers

import io.ktor.server.application.call
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.RoutingContext
import kotlinx.serialization.Serializable
import locapin.admin.models.RecordStatus
import locapin.admin.services.AdminContentService
import locapin.admin.services.FileStorageService
import locapin.admin.templates.renderView

class AttractionController(
    private val contentService: AdminContentService,
    private val fileStorageService: FileStorageService
) {
    suspend fun list(ctx: RoutingContext) {
        val cities = contentService.listCities()
        val cityId = ctx.call.request.queryParameters["cityId"]?.toLongOrNull()
        val areaId = ctx.call.request.queryParameters["areaId"]?.toLongOrNull()
        val search = ctx.call.request.queryParameters["search"]
        val rows = contentService.listAttractions(cityId, areaId, search)
        ctx.call.renderView(
            "admin/attractions-list.html",
            mapOf("pageTitle" to "Attractions", "activeNav" to "attractions", "cities" to cities, "attractions" to rows, "selectedCityId" to cityId, "search" to search.orEmpty())
        )
    }

    suspend fun form(ctx: RoutingContext, attractionFormId: Long?) {
        val attraction = attractionFormId?.let { contentService.getAttraction(it) }
        val cities = contentService.listCities()
        val areas = attraction?.let { contentService.areasByCity(it.cityId) } ?: cities.firstOrNull()?.let { contentService.areasByCity(it.id) }.orEmpty()
        ctx.call.renderView(
            "admin/attraction-form.html",
            mapOf(
                "pageTitle" to if (attractionFormId == null) "Create Attraction" else "Edit Attraction",
                "activeNav" to "attractions",
                "attraction" to attraction,
                "cities" to cities,
                "areas" to areas,
                "statuses" to RecordStatus.entries
            )
        )
    }

    suspend fun save(ctx: RoutingContext, id: Long?) {
        val p = ctx.call.receiveParameters()
        contentService.saveAttraction(
            id,
            cityId = p["cityId"]!!.toLong(),
            areaId = p["areaId"]!!.toLong(),
            name = p["name"].orEmpty(),
            description = p["description"].orEmpty(),
            highlights = p["highlights"].orEmpty(),
            lat = p["lat"]!!.toDouble(),
            lng = p["lng"]!!.toDouble(),
            openHours = p["openHours"],
            status = RecordStatus.valueOf(p["status"].orEmpty()),
            isFeatured = p["isFeatured"] == "on"
        )
        ctx.call.respondRedirect("/admin/attractions")
    }

    suspend fun detail(ctx: RoutingContext, id: Long) {
        val attr = contentService.getAttraction(id) ?: return ctx.call.respondRedirect("/admin/attractions")
        val photos = contentService.photos(id)
        ctx.call.renderView("admin/attraction-detail.html", mapOf("pageTitle" to "Attraction Detail", "activeNav" to "attractions", "attraction" to attr, "photos" to photos))
    }

    suspend fun archive(ctx: RoutingContext, id: Long) {
        contentService.archiveAttraction(id)
        ctx.call.respondRedirect("/admin/attractions")
    }

    suspend fun photosPage(ctx: RoutingContext, attractionId: Long) {
        val photos = contentService.photos(attractionId)
        ctx.call.renderView("admin/photo-management.html", mapOf("pageTitle" to "Photo Management", "activeNav" to "attractions", "attractionId" to attractionId, "photos" to photos))
    }

    suspend fun uploadPhotos(ctx: RoutingContext, attractionId: Long) {
        val images = fileStorageService.saveImages(ctx.call)
        images.forEachIndexed { index, path -> contentService.addPhoto(attractionId, path, index + 1) }
        ctx.call.respondRedirect("/admin/attractions/$attractionId/photos")
    }

    suspend fun deletePhoto(ctx: RoutingContext, photoId: Long) {
        contentService.deletePhoto(photoId)
        ctx.call.respond(mapOf("ok" to true))
    }

    @Serializable data class ReorderPayload(val photoOrders: List<PhotoOrder>)
    @Serializable data class PhotoOrder(val photoId: Long, val sortOrder: Int)
}
