package locapin.admin.controllers

import io.ktor.server.application.call
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.RoutingContext
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import kotlinx.serialization.Serializable
import locapin.admin.auth.AdminSession
import locapin.admin.models.RecordStatus
import locapin.admin.services.AdminContentService
import locapin.admin.services.FileStorageService
import locapin.admin.templates.renderPage

class AttractionController(
    private val contentService: AdminContentService,
    private val fileStorageService: FileStorageService
) {
    suspend fun list(ctx: RoutingContext) {
        val session = ctx.call.sessions.get<AdminSession>()
        val cities = contentService.listCities()
        val cityId = ctx.call.request.queryParameters["cityId"]?.toLongOrNull()
        val areaId = ctx.call.request.queryParameters["areaId"]?.toLongOrNull()
        val search = ctx.call.request.queryParameters["search"]
        val rows = contentService.listAttractions(cityId, areaId, search)
        ctx.call.renderPage("Attractions", session) {
            a("/admin/attractions/new", classes = "btn") { +"Add Attraction" }
            form("/admin/attractions", kotlinx.html.FormMethod.get) {
                textInput(name = "search") { placeholder = "Search by name"; value = search ?: "" }
                select { name = "cityId"; option { value = ""; +"All cities" }; cities.forEach { c -> option { value = c.id.toString(); +c.name } } }
                button { +"Apply" }
            }
            table {
                tr { th { +"Name" }; th { +"City" }; th { +"Area" }; th { +"Status" }; th { +"Featured" }; th { +"Actions" } }
                rows.forEach { r -> tr { td { +r.name }; td { +r.cityName }; td { +r.areaName }; td { +r.status.name }; td { +r.isFeatured.toString() }; td {
                    a("/admin/attractions/${r.id}") { +"View" }; +" | "; a("/admin/attractions/${r.id}/edit") { +"Edit" }
                } } }
            }
        }
    }

    suspend fun form(ctx: RoutingContext, id: Long?) {
        val session = ctx.call.sessions.get<AdminSession>()
        val attraction = id?.let { contentService.getAttraction(it) }
        val cities = contentService.listCities()
        val areas = attraction?.let { contentService.areasByCity(it.cityId) } ?: cities.firstOrNull()?.let { contentService.areasByCity(it.id) }.orEmpty()
        ctx.call.renderPage(if (id == null) "Create Attraction" else "Edit Attraction", session) {
            form(if (id == null) "/admin/attractions" else "/admin/attractions/$id", kotlinx.html.FormMethod.post) {
                label { +"City" }; select { name = "cityId"; id = "citySelect"; attributes["data-area-target"] = "areaSelect"; cities.forEach { c -> option { value = c.id.toString(); +c.name; if (attraction?.cityId == c.id) selected = true } } }
                label { +"Area" }; select { name = "areaId"; id = "areaSelect"; areas.forEach { a -> option { value = a.id.toString(); +a.name; if (attraction?.areaId == a.id) selected = true } } }
                label { +"Name" }; textInput(name = "name") { value = attraction?.name ?: ""; required = true }
                label { +"Description" }; textArea { name = "description"; +(attraction?.description ?: "") }
                label { +"Highlights" }; textArea { name = "highlights"; +(attraction?.highlights ?: "") }
                label { +"Latitude" }; numberInput(name = "lat") { step = "any"; value = attraction?.latitude?.toString() ?: "" }
                label { +"Longitude" }; numberInput(name = "lng") { step = "any"; value = attraction?.longitude?.toString() ?: "" }
                label { +"Open Hours" }; textInput(name = "openHours") { value = attraction?.openHours ?: "" }
                label { +"Featured" }; checkBoxInput(name = "isFeatured") { checked = attraction?.isFeatured == true }
                label { +"Status" }; select { name = "status"; RecordStatus.entries.forEach { s -> option { value = s.name; +s.name; if (attraction?.status == s || (attraction == null && s == RecordStatus.ACTIVE)) selected = true } } }
                button { +"Save" }
            }
        }
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
        val session = ctx.call.sessions.get<AdminSession>()
        val attr = contentService.getAttraction(id) ?: return ctx.call.respondRedirect("/admin/attractions")
        val photos = contentService.photos(id)
        ctx.call.renderPage("Attraction Detail", session) {
            h3 { +attr.name }
            p { +attr.description }
            p { +"Highlights: ${attr.highlights}" }
            a("/admin/attractions/$id/photos", classes = "btn") { +"Manage Photos" }
            form("/admin/attractions/$id/archive", kotlinx.html.FormMethod.post) { button(classes = "btn-danger") { +"Archive attraction" } }
            div("gallery") { photos.forEach { img(src = it.imagePath, alt = "photo") { classes = setOf("preview") } } }
        }
    }

    suspend fun archive(ctx: RoutingContext, id: Long) {
        contentService.archiveAttraction(id)
        ctx.call.respondRedirect("/admin/attractions")
    }

    suspend fun photosPage(ctx: RoutingContext, attractionId: Long) {
        val session = ctx.call.sessions.get<AdminSession>()
        val photos = contentService.photos(attractionId)
        ctx.call.renderPage("Photo Management", session) {
            form("/admin/attractions/$attractionId/photos", kotlinx.html.FormMethod.post, encType = kotlinx.html.FormEncType.multipartFormData) {
                fileInput(name = "photos") { multiple = true; accept = "image/*"; id = "photoInput" }
                div { id = "previewContainer" }
                button { +"Upload" }
            }
            ul {
                photos.forEach { p ->
                    li {
                        +"#${p.sortOrder} "
                        img(src = p.imagePath, alt = "photo") { classes = setOf("thumb") }
                        form("/admin/photos/${p.id}/delete", kotlinx.html.FormMethod.post) { button(classes = "btn-danger") { +"Delete" } }
                    }
                }
            }
        }
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
