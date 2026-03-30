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
import locapin.admin.templates.HtmlViewEngine
import locapin.admin.templates.HtmlViewEngine.RawHtml
import locapin.admin.templates.renderTemplate

class AttractionController(
    private val contentService: AdminContentService,
    private val fileStorageService: FileStorageService
) {
    suspend fun list(ctx: RoutingContext) {
        val cities = contentService.listCities()
        val selectedCityId = ctx.call.request.queryParameters["cityId"]?.toLongOrNull()
        val search = ctx.call.request.queryParameters["search"].orEmpty()
        val rows = contentService.listAttractions(selectedCityId, null, search)

        val cityOptions = RawHtml(cities.joinToString("") {
            val s = if (selectedCityId == it.id) "selected" else ""
            "<option value='${it.id}' $s>${HtmlViewEngine.escape(it.name)}</option>"
        })
        val attractionRows = RawHtml(rows.joinToString("") {
            "<tr><td>${HtmlViewEngine.escape(it.name)}</td><td>${HtmlViewEngine.escape(it.cityName)}</td><td>${HtmlViewEngine.escape(it.areaName)}</td><td>${it.status}</td><td>${if (it.isFeatured) "Yes" else "No"}</td><td><a href='/admin/attractions/${it.id}'>View</a> | <a href='/admin/attractions/${it.id}/edit'>Edit</a></td></tr>"
        })

        ctx.call.renderTemplate("admin/attractions-list.html", mapOf("pageTitle" to "Attractions", "activeNav" to "attractions", "search" to search, "cityOptions" to cityOptions, "attractionRows" to attractionRows))
    }

    suspend fun form(ctx: RoutingContext, attractionFormId: Long?) {
        val attraction = attractionFormId?.let { contentService.getAttraction(it) }
        val cities = contentService.listCities()
        val areas = attraction?.let { contentService.areasByCity(it.cityId) } ?: cities.firstOrNull()?.let { contentService.areasByCity(it.id) }.orEmpty()

        val cityOptions = RawHtml(cities.joinToString("") {
            val s = if (attraction?.cityId == it.id) "selected" else ""
            "<option value='${it.id}' $s>${HtmlViewEngine.escape(it.name)}</option>"
        })
        val areaOptions = RawHtml(areas.joinToString("") {
            val s = if (attraction?.areaId == it.id) "selected" else ""
            "<option value='${it.id}' $s>${HtmlViewEngine.escape(it.name)}</option>"
        })
        val statusOptions = RawHtml(RecordStatus.entries.joinToString("") {
            val s = if ((attraction?.status ?: RecordStatus.ACTIVE) == it) "selected" else ""
            "<option value='$it' $s>$it</option>"
        })

        ctx.call.renderTemplate("admin/attraction-form.html", mapOf(
            "pageTitle" to if (attractionFormId == null) "Create Attraction" else "Edit Attraction",
            "activeNav" to "attractions",
            "formAction" to if (attractionFormId == null) "/admin/attractions" else "/admin/attractions/$attractionFormId",
            "cityOptions" to cityOptions,
            "areaOptions" to areaOptions,
            "attractionName" to (attraction?.name ?: ""),
            "attractionDescription" to (attraction?.description ?: ""),
            "attractionHighlights" to (attraction?.highlights ?: ""),
            "attractionLat" to (attraction?.latitude?.toString() ?: ""),
            "attractionLng" to (attraction?.longitude?.toString() ?: ""),
            "attractionOpenHours" to (attraction?.openHours ?: ""),
            "featuredChecked" to if (attraction?.isFeatured == true) "checked" else "",
            "statusOptions" to statusOptions
        ))
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
        val photoGallery = RawHtml(photos.joinToString("") { "<img src='${HtmlViewEngine.escape(it.imagePath)}' alt='photo' class='preview' />" })
        ctx.call.renderTemplate("admin/attraction-detail.html", mapOf(
            "pageTitle" to "Attraction Detail",
            "activeNav" to "attractions",
            "attractionName" to attr.name,
            "attractionDescription" to attr.description,
            "attractionHighlights" to attr.highlights,
            "attractionId" to attr.id,
            "photoGallery" to photoGallery
        ))
    }

    suspend fun archive(ctx: RoutingContext, id: Long) {
        contentService.archiveAttraction(id)
        ctx.call.respondRedirect("/admin/attractions")
    }

    suspend fun photosPage(ctx: RoutingContext, attractionId: Long) {
        val photos = contentService.photos(attractionId)
        val photoRows = RawHtml(photos.joinToString("") {
            "<li>#${it.sortOrder} <img src='${HtmlViewEngine.escape(it.imagePath)}' class='thumb' alt='photo' /> <form action='/admin/photos/${it.id}/delete' method='post'><button type='submit' class='btn-danger'>Delete</button></form></li>"
        })
        ctx.call.renderTemplate("admin/photos.html", mapOf("pageTitle" to "Photo Management", "activeNav" to "attractions", "attractionId" to attractionId, "photoRows" to photoRows))
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
