package locapin.admin.models

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object AdminUsersTable : Table("admin_users") {
    val id = long("id").autoIncrement()
    val fullName = varchar("full_name", 120)
    // Backward-compatibility column for legacy deployments.
    val legacyName = varchar("name", 120).nullable()
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 100)
    val role = enumerationByName("role", 30, AdminRole::class)
    val status = enumerationByName("status", 20, AdminAccountStatus::class).default(AdminAccountStatus.ACTIVE)
    val createdBy = long("created_by").nullable()
    val lastLoginAt = timestamp("last_login_at").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    override val primaryKey = PrimaryKey(id)
}

object AdminPermissionsTable : Table("admin_permissions") {
    val id = long("id").autoIncrement()
    val adminUserId = long("admin_user_id").references(AdminUsersTable.id, onDelete = ReferenceOption.CASCADE)
    val moduleKey = enumerationByName("module_key", 40, ModuleKey::class)
    val canCreate = bool("can_create").default(false)
    val canRead = bool("can_read").default(false)
    val canUpdate = bool("can_update").default(false)
    val canDelete = bool("can_delete").default(false)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    init { uniqueIndex(adminUserId, moduleKey) }
    override val primaryKey = PrimaryKey(id)
}

object CitiesTable : Table("cities") {
    val id = long("id").autoIncrement()
    val name = varchar("name", 120).uniqueIndex()
    val isPremium = bool("is_premium").default(false)
    val status = enumerationByName("status", 20, EntityStatus::class).default(EntityStatus.ACTIVE)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    override val primaryKey = PrimaryKey(id)
}

object AreasTable : Table("areas") {
    val id = long("id").autoIncrement()
    val cityId = long("city_id").references(CitiesTable.id, onDelete = ReferenceOption.RESTRICT)
    val name = varchar("name", 120)
    val centerLatitude = double("center_latitude")
    val centerLongitude = double("center_longitude")
    val boundaryData = text("boundary_data").nullable()
    val status = enumerationByName("status", 20, EntityStatus::class).default(EntityStatus.ACTIVE)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    init { uniqueIndex(cityId, name) }
    override val primaryKey = PrimaryKey(id)
}

object AttractionsTable : Table("attractions") {
    val id = long("id").autoIncrement()
    val cityId = long("city_id").references(CitiesTable.id, onDelete = ReferenceOption.RESTRICT)
    val areaId = long("area_id").references(AreasTable.id, onDelete = ReferenceOption.RESTRICT)
    val name = varchar("name", 180)
    val description = text("description")
    val highlights = text("highlights")
    val latitude = double("latitude")
    val longitude = double("longitude")
    val openHours = varchar("open_hours", 255).nullable()
    val status = enumerationByName("status", 20, EntityStatus::class).default(EntityStatus.ACTIVE)
    val isFeatured = bool("is_featured").default(false)
    val isDeleted = bool("is_deleted").default(false)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    override val primaryKey = PrimaryKey(id)
}

object AttractionPhotosTable : Table("attraction_photos") {
    val id = long("id").autoIncrement()
    val attractionId = long("attraction_id").references(AttractionsTable.id, onDelete = ReferenceOption.CASCADE)
    val imagePath = varchar("image_path", 400)
    val sortOrder = integer("sort_order")
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}

object UsersTable : Table("users") {
    val id = long("id").autoIncrement()
    val fullName = varchar("full_name", 120)
    val email = varchar("email", 255).uniqueIndex()
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}

object SubscriptionPlansTable : Table("subscription_plans") {
    val id = long("id").autoIncrement()
    val name = varchar("name", 120).uniqueIndex()
    val description = text("description")
    val price = decimal("price", 10, 2)
    val billingPeriod = enumerationByName("billing_period", 20, BillingPeriod::class)
    val isActive = bool("is_active").default(true)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    override val primaryKey = PrimaryKey(id)
}

object SubscriptionsTable : Table("subscriptions") {
    val id = long("id").autoIncrement()
    val userId = long("user_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val planId = long("plan_id").references(SubscriptionPlansTable.id, onDelete = ReferenceOption.RESTRICT)
    val isActive = bool("is_active").default(true)
    val startedAt = timestamp("started_at")
    val expiresAt = timestamp("expires_at").nullable()
    override val primaryKey = PrimaryKey(id)
}
