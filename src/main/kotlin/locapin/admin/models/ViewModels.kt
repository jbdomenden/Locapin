package locapin.admin.models

import java.math.BigDecimal
import java.time.LocalDateTime

data class AdminUser(val id: Long, val fullName: String, val email: String, val passwordHash: String, val role: AdminRole, val status: RecordStatus)
data class CityView(val id: Long, val name: String, val isPremium: Boolean, val status: RecordStatus, val createdAt: LocalDateTime)
data class AreaView(val id: Long, val cityId: Long, val cityName: String, val name: String, val centerLatitude: Double, val centerLongitude: Double, val status: RecordStatus)
data class AttractionView(val id: Long, val cityId: Long, val cityName: String, val areaId: Long, val areaName: String, val name: String, val description: String, val highlights: String, val latitude: Double, val longitude: Double, val openHours: String?, val status: RecordStatus, val isFeatured: Boolean, val createdAt: LocalDateTime)
data class PhotoView(val id: Long, val attractionId: Long, val imagePath: String, val sortOrder: Int)
data class PlanView(val id: Long, val name: String, val description: String, val price: BigDecimal, val billingPeriod: BillingPeriod, val isActive: Boolean)

data class DashboardStats(
    val totalCities: Long,
    val totalAreas: Long,
    val totalAttractions: Long,
    val totalPhotos: Long,
    val totalUsers: Long,
    val totalPremiumSubscribers: Long,
    val featuredAttractions: Long,
    val activeAttractions: Long,
    val inactiveAttractions: Long,
    val latestAttractions: List<AttractionView>
)
