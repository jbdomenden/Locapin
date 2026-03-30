package locapin.admin.services

import io.ktor.server.plugins.BadRequestException
import locapin.admin.models.*
import locapin.admin.repositories.ContentRepository
import locapin.admin.utils.Validators

class AdminService(private val repo: ContentRepository = ContentRepository()) {
    fun dashboard() = repo.dashboardStats()
    fun listCities() = repo.listCities()
    fun getCity(id: Long) = repo.getCity(id)
    fun createCity(req: CityRequest): Long { Validators.requireNotBlank(req.name, "City name"); return repo.createCity(req) }
    fun updateCity(id: Long, req: CityRequest) { Validators.requireNotBlank(req.name, "City name"); repo.updateCity(id, req) }
    fun updateCityStatus(id: Long, status: EntityStatus) = repo.updateCityStatus(id, status)
    fun updateCityPremium(id: Long, premium: Boolean) = repo.updateCityPremium(id, premium)

    fun listAreas(cityId: Long?) = repo.listAreas(cityId)
    fun getArea(id: Long) = repo.getArea(id)
    fun areasByCity(cityId: Long) = repo.listAreas(cityId)
    fun createArea(req: AreaRequest): Long { Validators.requireNotBlank(req.name, "Area name"); Validators.validateLatLng(req.centerLatitude, req.centerLongitude); return repo.createArea(req) }
    fun updateArea(id: Long, req: AreaRequest) { Validators.requireNotBlank(req.name, "Area name"); Validators.validateLatLng(req.centerLatitude, req.centerLongitude); repo.updateArea(id, req) }
    fun updateAreaStatus(id: Long, status: EntityStatus) = repo.updateAreaStatus(id, status)

    fun listAttractions(cityId: Long?, areaId: Long?, q: String?) = repo.listAttractions(cityId, areaId, q)
    fun getAttraction(id: Long) = repo.getAttraction(id)
    fun createAttraction(req: AttractionRequest): Long {
        Validators.requireNotBlank(req.name, "Attraction name"); Validators.requireNotBlank(req.description, "Description"); Validators.requireNotBlank(req.highlights, "Highlights"); Validators.validateLatLng(req.latitude, req.longitude)
        val area = repo.getArea(req.areaId) ?: throw BadRequestException("Area not found")
        if ((area["cityId"] as Long) != req.cityId) throw BadRequestException("Area does not belong to selected city")
        return repo.createAttraction(req)
    }
    fun updateAttraction(id: Long, req: AttractionRequest) {
        Validators.requireNotBlank(req.name, "Attraction name"); Validators.requireNotBlank(req.description, "Description"); Validators.requireNotBlank(req.highlights, "Highlights"); Validators.validateLatLng(req.latitude, req.longitude)
        repo.updateAttraction(id, req)
    }
    fun updateAttractionStatus(id: Long, status: EntityStatus) = repo.updateAttractionStatus(id, status)
    fun updateAttractionFeatured(id: Long, featured: Boolean) = repo.updateAttractionFeatured(id, featured)
    fun deleteAttraction(id: Long) = repo.softDeleteAttraction(id)

    fun listPlans() = repo.listPlans()
    fun getPlan(id: Long) = repo.getPlan(id)
    fun createPlan(req: PlanRequest): Long { Validators.requireNotBlank(req.name, "Plan name"); Validators.requireNotBlank(req.description, "Plan description"); Validators.validatePrice(req.price); return repo.createPlan(req) }
    fun updatePlan(id: Long, req: PlanRequest) { Validators.validatePrice(req.price); repo.updatePlan(id, req) }
    fun updatePlanStatus(id: Long, active: Boolean) = repo.updatePlanStatus(id, active)

    fun listPhotos(attractionId: Long) = repo.listPhotos(attractionId)
    fun addPhoto(attractionId: Long, path: String, sortOrder: Int) = repo.addPhoto(attractionId, path, sortOrder)
    fun reorderPhotos(items: List<PhotoReorderItem>) {
        if (items.map { it.sortOrder }.any { it < 0 }) throw BadRequestException("Sort order must be non-negative")
        repo.reorderPhotos(items)
    }
    fun deletePhoto(id: Long) = repo.deletePhoto(id)
}
