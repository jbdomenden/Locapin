package locapin.admin.models

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.javatime.date

object AdminUsers : LongIdTable("admin_users") {
    val fullName = varchar("full_name", 150)
    val email = varchar("email", 180).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val role = enumerationByName("role", 32, AdminRole::class)
    val status = enumerationByName("status", 32, RecordStatus::class).default(RecordStatus.ACTIVE)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

object Cities : LongIdTable("cities") {
    val name = varchar("name", 120).uniqueIndex()
    val isPremium = bool("is_premium").default(false)
    val status = enumerationByName("status", 32, RecordStatus::class).default(RecordStatus.ACTIVE)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

object Areas : LongIdTable("areas") {
    val cityId = reference("city_id", Cities)
    val name = varchar("name", 120)
    val centerLatitude = double("center_latitude")
    val centerLongitude = double("center_longitude")
    val boundaryData = text("boundary_data").nullable()
    val status = enumerationByName("status", 32, RecordStatus::class).default(RecordStatus.ACTIVE)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    init {
        uniqueIndex(cityId, name)
    }
}

object Attractions : LongIdTable("attractions") {
    val cityId = reference("city_id", Cities)
    val areaId = reference("area_id", Areas)
    val name = varchar("name", 180)
    val description = text("description")
    val highlights = text("highlights")
    val latitude = double("latitude")
    val longitude = double("longitude")
    val openHours = varchar("open_hours", 120).nullable()
    val status = enumerationByName("status", 32, RecordStatus::class).default(RecordStatus.ACTIVE)
    val isFeatured = bool("is_featured").default(false)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

object AttractionPhotos : LongIdTable("attraction_photos") {
    val attractionId = reference("attraction_id", Attractions)
    val imagePath = varchar("image_path", 300)
    val sortOrder = integer("sort_order").default(0)
    val createdAt = datetime("created_at")
}

object Users : LongIdTable("users") {
    val name = varchar("name", 120)
    val email = varchar("email", 180).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val subscriptionType = enumerationByName("subscription_type", 24, UserSubscriptionType::class)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

object SubscriptionPlans : LongIdTable("subscription_plans") {
    val name = varchar("name", 120).uniqueIndex()
    val description = text("description")
    val price = decimal("price", 10, 2)
    val billingPeriod = enumerationByName("billing_period", 24, BillingPeriod::class)
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

object Subscriptions : LongIdTable("subscriptions") {
    val userId = reference("user_id", Users)
    val planId = reference("plan_id", SubscriptionPlans)
    val status = enumerationByName("status", 24, SubscriptionStatus::class)
    val startDate = date("start_date")
    val endDate = date("end_date").nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}
