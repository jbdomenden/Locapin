package locapin.admin.services

import locapin.admin.models.*
import locapin.admin.repositories.*
import locapin.admin.utils.validateLatLng
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.LocalDateTime

class AdminContentService(
    private val cityRepo: CityRepository = CityRepository(),
    private val areaRepo: AreaRepository = AreaRepository(),
    private val attractionRepo: AttractionRepository = AttractionRepository(),
    private val photoRepo: PhotoRepository = PhotoRepository(),
    private val planRepo: PlanRepository = PlanRepository(),
    private val analyticsRepo: AnalyticsRepository = AnalyticsRepository()
) {
    fun dashboardStats(): DashboardStats = transaction {
        analyticsRepo.dashboardStats(attractionRepo.latest(5))
    }

    fun listCities() = transaction { cityRepo.all() }
    fun getCity(id: Long) = transaction { cityRepo.get(id) }
    fun saveCity(id: Long?, name: String, isPremium: Boolean, status: RecordStatus) = transaction {
        val now = LocalDateTime.now()
        if (id == null) cityRepo.create(name, isPremium, status, now)
        else cityRepo.update(id, name, isPremium, status, now)
    }

    fun listAreas(cityId: Long?) = transaction { areaRepo.all(cityId) }
    fun getArea(id: Long) = transaction { areaRepo.get(id) }
    fun areasByCity(cityId: Long) = transaction { areaRepo.byCity(cityId) }
    fun saveArea(id: Long?, cityId: Long, name: String, lat: Double, lng: Double, boundary: String?, status: RecordStatus) = transaction {
        validateLatLng(lat, lng)
        val now = LocalDateTime.now()
        if (id == null) areaRepo.create(cityId, name, lat, lng, boundary, status, now)
        else areaRepo.update(id, cityId, name, lat, lng, boundary, status, now)
    }

    fun listAttractions(cityId: Long?, areaId: Long?, search: String?) = transaction { attractionRepo.all(cityId, areaId, search) }
    fun getAttraction(id: Long) = transaction { attractionRepo.get(id) }
    fun saveAttraction(id: Long?, cityId: Long, areaId: Long, name: String, description: String, highlights: String, lat: Double, lng: Double, openHours: String?, status: RecordStatus, isFeatured: Boolean) = transaction {
        validateLatLng(lat, lng)
        val now = LocalDateTime.now()
        if (id == null) attractionRepo.create(cityId, areaId, name, description, highlights, lat, lng, openHours, status, isFeatured, now)
        else attractionRepo.update(id, cityId, areaId, name, description, highlights, lat, lng, openHours, status, isFeatured, now)
    }

    fun archiveAttraction(id: Long) = transaction { attractionRepo.softDelete(id, LocalDateTime.now()) }
    fun photos(attractionId: Long) = transaction { photoRepo.forAttraction(attractionId) }
    fun addPhoto(attractionId: Long, path: String, sortOrder: Int) = transaction { photoRepo.create(attractionId, path, sortOrder, LocalDateTime.now()) }
    fun deletePhoto(id: Long) = transaction { photoRepo.delete(id) }
    fun reorderPhotos(orders: Map<Long, Int>) = transaction { photoRepo.reorder(orders) }

    fun listPlans() = transaction { planRepo.all() }
    fun getPlan(id: Long) = transaction { planRepo.get(id) }
    fun savePlan(id: Long?, name: String, description: String, price: BigDecimal, period: BillingPeriod, isActive: Boolean) = transaction {
        val now = LocalDateTime.now()
        if (id == null) planRepo.create(name, description, price, period, isActive, now)
        else planRepo.update(id, name, description, price, period, isActive, now)
    }

    fun attractionsPerCity() = transaction { analyticsRepo.attractionsPerCity() }
    fun attractionsPerArea() = transaction { analyticsRepo.attractionsPerArea() }
}
