package locapin.admin.routes

import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import locapin.admin.auth.requireAdminSession
import locapin.admin.config.AppConfig
import locapin.admin.controllers.AdminController
import locapin.admin.controllers.AttractionController
import locapin.admin.services.AdminContentService
import locapin.admin.services.FileStorageService

fun Application.configureRoutes(config: AppConfig) {
    val adminController = AdminController()
    val contentService = AdminContentService()
    val attractionController = AttractionController(contentService, FileStorageService(config.fileUploadDir))

    routing {
        get("/") { call.respondRedirect("/admin/login") }

        route("/admin") {
            get("/login") { adminController.loginPage(this) }
            post("/login") { adminController.login(this) }
            get("/logout") { adminController.logout(this) }

            route("") {
                requireAdminSession()
                get("/dashboard") { adminController.dashboard(this) }

                get("/cities") { adminController.cities(this) }
                get("/cities/new") { adminController.cityForm(this, null) }
                post("/cities") { adminController.saveCity(this, null) }
                get("/cities/{id}/edit") { adminController.cityForm(this, call.parameters["id"]!!.toLong()) }
                post("/cities/{id}") { adminController.saveCity(this, call.parameters["id"]!!.toLong()) }

                get("/areas") { adminController.areas(this) }
                get("/areas/new") { adminController.areaForm(this, null) }
                post("/areas") { adminController.saveArea(this, null) }
                get("/areas/{id}/edit") { adminController.areaForm(this, call.parameters["id"]!!.toLong()) }
                post("/areas/{id}") { adminController.saveArea(this, call.parameters["id"]!!.toLong()) }

                get("/attractions") { attractionController.list(this) }
                get("/attractions/new") { attractionController.form(this, null) }
                post("/attractions") { attractionController.save(this, null) }
                get("/attractions/{id}") { attractionController.detail(this, call.parameters["id"]!!.toLong()) }
                get("/attractions/{id}/edit") { attractionController.form(this, call.parameters["id"]!!.toLong()) }
                post("/attractions/{id}") { attractionController.save(this, call.parameters["id"]!!.toLong()) }
                post("/attractions/{id}/archive") { attractionController.archive(this, call.parameters["id"]!!.toLong()) }
                get("/attractions/{id}/photos") { attractionController.photosPage(this, call.parameters["id"]!!.toLong()) }
                post("/attractions/{id}/photos") { attractionController.uploadPhotos(this, call.parameters["id"]!!.toLong()) }

                get("/plans") { adminController.plans(this) }
                get("/plans/new") { adminController.planForm(this, null) }
                post("/plans") { adminController.savePlan(this, null) }
                get("/plans/{id}/edit") { adminController.planForm(this, call.parameters["id"]!!.toLong()) }
                post("/plans/{id}") { adminController.savePlan(this, call.parameters["id"]!!.toLong()) }

                post("/photos/{id}/delete") {
                    attractionController.deletePhoto(this, call.parameters["id"]!!.toLong())
                }

                route("/api") {
                    get("/areas/by-city/{cityId}") {
                        call.respond(contentService.areasByCity(call.parameters["cityId"]!!.toLong()))
                    }
                    get("/attractions/search") {
                        val cityId = call.request.queryParameters["cityId"]?.toLongOrNull()
                        val areaId = call.request.queryParameters["areaId"]?.toLongOrNull()
                        val query = call.request.queryParameters["q"]
                        call.respond(contentService.listAttractions(cityId, areaId, query))
                    }
                    post("/photos/reorder") {
                        val payload = call.receive<AttractionController.ReorderPayload>()
                        contentService.reorderPhotos(payload.photoOrders.associate { it.photoId to it.sortOrder })
                        call.respond(mapOf("ok" to true))
                    }
                    get("/dashboard/stats") {
                        call.respond(
                            mapOf(
                                "stats" to contentService.dashboardStats(),
                                "attractionsPerCity" to contentService.attractionsPerCity(),
                                "attractionsPerArea" to contentService.attractionsPerArea()
                            )
                        )
                    }
                }
            }
        }
    }
}
