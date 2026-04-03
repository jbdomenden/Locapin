package locapin.admin.services

import locapin.admin.config.AppConfig
import locapin.admin.models.*
import locapin.admin.repositories.AdminRepository
import locapin.admin.repositories.PermissionRecord
import locapin.admin.utils.Passwords
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.ChronoUnit

class BootstrapService(private val config: AppConfig) {
    private val adminRepository = AdminRepository()
    private val logger = LoggerFactory.getLogger(BootstrapService::class.java)

    fun bootstrap() {
        val superAdminEmail = config.adminInitialEmail.lowercase()
        val superAdminPassword = config.adminInitialPassword

        if (adminRepository.count() == 0L) {
            adminRepository.create(
                fullName = config.adminInitialName,
                email = superAdminEmail,
                passwordHash = Passwords.hash(superAdminPassword),
                role = AdminRole.SUPER_ADMIN
            )
            logger.info("Bootstrapped initial superadmin account.")
        }

        seedExtraAdminUsersIfNeeded()
        seedContentDataIfNeeded()
    }

    private fun seedExtraAdminUsersIfNeeded() {
        if (adminRepository.count() != 1L) return

        val superAdminId = adminRepository.findByEmail(config.adminInitialEmail.lowercase())?.id ?: return
        val adminId = adminRepository.create(
            fullName = "Maria Santos",
            email = "admin.test@locapin.local",
            passwordHash = Passwords.hash("AdminTest123!"),
            role = AdminRole.SUPER_ADMIN,
            createdBy = superAdminId
        )
        val moderatorId = adminRepository.create(
            fullName = "Paolo Reyes",
            email = "editor.test@locapin.local",
            passwordHash = Passwords.hash("EditorTest123!"),
            role = AdminRole.MODERATOR,
            createdBy = superAdminId
        )

        adminRepository.replacePermissions(
            adminId,
            ModuleKey.entries.map { PermissionRecord(it, true, true, true, true) }
        )
        adminRepository.replacePermissions(moderatorId, adminRepository.defaultModeratorPermissions())

        if (config.appEnv == "development") {
            logger.info("Seeded admin account -> email: admin.test@locapin.local | password: AdminTest123!")
            logger.info("Seeded editor account -> email: editor.test@locapin.local | password: EditorTest123!")
        }
    }

    private fun seedContentDataIfNeeded() = transaction {
        val now = Instant.now()

        val sanJuanCityId = ensureSanJuanCity(now)

        val areaIds = ensureAreas(sanJuanCityId, now)
        val attractionIds = ensureAttractions(sanJuanCityId, areaIds, now)

        val planIds = ensureSubscriptionPlans(now)
        val userIds = ensureSeedUsers(now)

        if (SubscriptionsTable.selectAll().count() == 0L && userIds.isNotEmpty() && planIds.isNotEmpty()) {
            val andreaId = userIds["Andrea Dela Cruz"]
            val premiumPlanId = planIds["Premium"]
            val miguelId = userIds["Miguel Tan"]
            val annualPremiumPlanId = planIds["Annual Premium"]

            if (andreaId == null || premiumPlanId == null || miguelId == null || annualPremiumPlanId == null) {
                logger.warn(
                    "Skipping subscription seed because required users/plans are missing. users={}, plans={}",
                    userIds.keys,
                    planIds.keys
                )
                return@transaction
            }

            SubscriptionsTable.insert {
                it[userId] = andreaId
                it[planId] = premiumPlanId
                it[isActive] = true
                it[startedAt] = now.minus(25, ChronoUnit.DAYS)
                it[expiresAt] = now.plus(5, ChronoUnit.DAYS)
            }
            SubscriptionsTable.insert {
                it[userId] = miguelId
                it[planId] = annualPremiumPlanId
                it[isActive] = true
                it[startedAt] = now.minus(60, ChronoUnit.DAYS)
                it[expiresAt] = now.plus(305, ChronoUnit.DAYS)
            }
        }

        val seededPhotos = seedAttractionPhotosIfAssetsExist(attractionIds, now)

        logger.info(
            "Bootstrap seed summary -> cities: {}, areas: {}, attractions: {}, plans: {}, users: {}, subscriptions: {}, photosInserted: {}",
            CitiesTable.selectAll().count(),
            AreasTable.selectAll().count(),
            AttractionsTable.selectAll().where { AttractionsTable.isDeleted eq false }.count(),
            SubscriptionPlansTable.selectAll().count(),
            UsersTable.selectAll().count(),
            SubscriptionsTable.selectAll().count(),
            seededPhotos
        )
    }

    private fun insertArea(cityId: Long, areaName: String, lat: Double, lng: Double, now: Instant): Long =
        AreasTable.insert {
            it[AreasTable.cityId] = cityId
            it[name] = areaName
            it[centerLatitude] = lat
            it[centerLongitude] = lng
            it[boundaryData] = null
            it[status] = EntityStatus.ACTIVE
            it[createdAt] = now
            it[updatedAt] = now
        }[AreasTable.id]

    private fun ensureSanJuanCity(now: Instant): Long =
        CitiesTable.selectAll().where { CitiesTable.name eq "San Juan City" }.singleOrNull()?.get(CitiesTable.id)
            ?: if (CitiesTable.selectAll().count() == 0L) {
                CitiesTable.insert {
                    it[name] = "San Juan City"
                    it[isPremium] = true
                    it[status] = EntityStatus.ACTIVE
                    it[createdAt] = now
                    it[updatedAt] = now
                }[CitiesTable.id]
            } else {
                CitiesTable.selectAll().limit(1).single()[CitiesTable.id]
            }

    private fun ensureAreas(cityId: Long, now: Instant): Map<String, Long> {
        val requiredAreas = listOf(
            AreaSeed("Greenhills", 14.6038, 121.0496),
            AreaSeed("Pinaglabanan", 14.6018, 121.0319),
            AreaSeed("Little Baguio", 14.5992, 121.0401),
            AreaSeed("West Crame", 14.6067, 121.0417),
            AreaSeed("Addition Hills", 14.5935, 121.0364)
        )

        return requiredAreas.associate { area ->
            val existingId = AreasTable.selectAll().where {
                (AreasTable.cityId eq cityId) and (AreasTable.name eq area.name)
            }.singleOrNull()?.get(AreasTable.id)
            area.name to (existingId ?: insertArea(cityId, area.name, area.lat, area.lng, now))
        }
    }

    private fun ensureAttractions(cityId: Long, areaIds: Map<String, Long>, now: Instant): Map<String, Long> {
        val requiredAttractions = listOf(
            AttractionSeed("Museo ng Katipunan", "Pinaglabanan", "A compact museum showcasing Katipunan memorabilia, local heritage archives, and guided exhibits.", "Historical artifacts, guided tours, educational displays", 14.6022, 121.0327, "Tue-Sun 9:00 AM - 5:00 PM", true),
            AttractionSeed("Pinaglabanan Shrine", "Pinaglabanan", "A key historical landmark that commemorates the Battle of San Juan del Monte.", "Heritage park, battle monument, open grounds", 14.6015, 121.0315, "Daily 6:00 AM - 8:00 PM", true),
            AttractionSeed("Ronac Art Center", "Greenhills", "A contemporary venue hosting rotating exhibitions from local and international artists.", "Modern exhibits, curated collections, art talks", 14.6048, 121.0523, "Wed-Mon 10:00 AM - 7:00 PM", false),
            AttractionSeed("Fundacion Sanso", "Greenhills", "Home of notable Philippine modern art pieces with regular curated exhibits.", "Museum quality galleries, artist archives, workshops", 14.5995, 121.0483, "Tue-Sun 10:00 AM - 6:00 PM", true),
            AttractionSeed("Art Sector Gallery", "Addition Hills", "A design-forward gallery spotlighting modern Filipino visual artists.", "Contemporary artwork, weekend events, artist meetups", 14.5952, 121.0378, "Thu-Tue 11:00 AM - 7:00 PM", false),
            AttractionSeed("Greenhills Shopping Center", "Greenhills", "A landmark shopping district known for dining, retail, and lifestyle stores.", "Shopping complex, food choices, local finds", 14.6029, 121.0498, "Daily 10:00 AM - 9:00 PM", true),
            AttractionSeed("Greenhills Promenade", "Greenhills", "Open-air promenade zone with entertainment, cinema, and cafes.", "Al fresco spaces, cinema, coffee spots", 14.6037, 121.0509, "Daily 10:00 AM - 10:00 PM", false),
            AttractionSeed("V-Mall", "Greenhills", "A vibrant mall wing popular for gadgets, fashion boutiques, and specialty stores.", "Electronics stores, fashion stalls, weekend foot traffic", 14.6033, 121.0501, "Daily 10:00 AM - 9:00 PM", true)
        )

        return requiredAttractions.associate { attraction ->
            val areaId = areaIds.getValue(attraction.areaName)
            val existingId = AttractionsTable.selectAll().where {
                (AttractionsTable.cityId eq cityId) and (AttractionsTable.name eq attraction.name)
            }.limit(1).singleOrNull()?.get(AttractionsTable.id)
            attraction.name to (existingId ?: insertAttraction(
                cityId = cityId,
                areaId = areaId,
                attractionName = attraction.name,
                descriptionValue = attraction.description,
                highlightsValue = attraction.highlights,
                lat = attraction.lat,
                lng = attraction.lng,
                openHoursValue = attraction.openHours,
                featured = attraction.featured,
                now = now
            ))
        }
    }

    private fun insertAttraction(
        cityId: Long,
        areaId: Long,
        attractionName: String,
        descriptionValue: String,
        highlightsValue: String,
        lat: Double,
        lng: Double,
        openHoursValue: String?,
        featured: Boolean,
        now: Instant
    ): Long = AttractionsTable.insert {
        it[AttractionsTable.cityId] = cityId
        it[AttractionsTable.areaId] = areaId
        it[name] = attractionName
        it[description] = descriptionValue
        it[highlights] = highlightsValue
        it[latitude] = lat
        it[longitude] = lng
        it[openHours] = openHoursValue
        it[status] = EntityStatus.ACTIVE
        it[isFeatured] = featured
        it[isDeleted] = false
        it[createdAt] = now
        it[updatedAt] = now
    }[AttractionsTable.id]

    private fun insertPlan(name: String, description: String, price: BigDecimal, period: BillingPeriod, active: Boolean, now: Instant): Long =
        SubscriptionPlansTable.insert {
            it[SubscriptionPlansTable.name] = name
            it[SubscriptionPlansTable.description] = description
            it[SubscriptionPlansTable.price] = price
            it[billingPeriod] = period
            it[isActive] = active
            it[createdAt] = now
            it[updatedAt] = now
        }[SubscriptionPlansTable.id]

    private fun ensureSubscriptionPlans(now: Instant): Map<String, Long> {
        val requiredPlans = listOf(
            PlanSeed("Explorer", "Entry plan for casual travelers with curated city guides and attraction updates.", BigDecimal("199.00"), BillingPeriod.MONTHLY, true),
            PlanSeed("Premium", "Enhanced traveler plan with premium city access and priority in-app support.", BigDecimal("499.00"), BillingPeriod.MONTHLY, true),
            PlanSeed("Annual Premium", "Annual savings plan for frequent travelers with full premium benefits.", BigDecimal("4990.00"), BillingPeriod.YEARLY, true)
        )

        return requiredPlans.associate { plan ->
            val existingId = SubscriptionPlansTable.selectAll().where { SubscriptionPlansTable.name eq plan.name }
                .limit(1)
                .singleOrNull()
                ?.get(SubscriptionPlansTable.id)
            plan.name to (existingId
                ?: insertPlan(plan.name, plan.description, plan.price, plan.period, plan.active, now))
        }
    }

    private fun insertUser(fullName: String, email: String, createdAt: Instant): Long =
        UsersTable.insert {
            it[UsersTable.fullName] = fullName
            it[UsersTable.email] = email
            it[UsersTable.createdAt] = createdAt
        }[UsersTable.id]

    private fun ensureSeedUsers(now: Instant): Map<String, Long> {
        val requiredUsers = listOf(
            UserSeed("Andrea Dela Cruz", "andrea.delacruz@example.com", now.minus(7, ChronoUnit.DAYS)),
            UserSeed("Miguel Tan", "miguel.tan@example.com", now.minus(5, ChronoUnit.DAYS)),
            UserSeed("Jasmine Flores", "jasmine.flores@example.com", now.minus(2, ChronoUnit.DAYS))
        )

        return requiredUsers.associate { user ->
            val existingId = UsersTable.selectAll().where { UsersTable.fullName eq user.fullName }
                .limit(1)
                .singleOrNull()
                ?.get(UsersTable.id)
            user.fullName to (existingId ?: insertUser(user.fullName, user.email, user.createdAt))
        }
    }

    private fun seedAttractionPhotosIfAssetsExist(attractionIds: Map<String, Long>, now: Instant): Int {
        if (AttractionPhotosTable.selectAll().count() > 0L) return 0

        val candidatePath = "src/main/resources/static/images/locapin-logo.jpg"
        val photoAsset = java.io.File(candidatePath)
        if (!photoAsset.exists()) {
            logger.warn("Skipping attraction photo bootstrap because no local seed image asset exists: {}", candidatePath)
            return 0
        }

        val targetAttractions = listOf(
            "Museo ng Katipunan",
            "Pinaglabanan Shrine",
            "Greenhills Shopping Center",
            "V-Mall"
        )

        var inserted = 0
        targetAttractions.forEachIndexed { index, attractionName ->
            val attractionId = attractionIds[attractionName]
            if (attractionId == null) {
                logger.warn("Skipping photo seed for attraction '{}' because it was not found in seeded attraction ids.", attractionName)
                return@forEachIndexed
            }
            AttractionPhotosTable.insert {
                it[AttractionPhotosTable.attractionId] = attractionId
                it[imagePath] = "/static/images/locapin-logo.jpg"
                it[sortOrder] = index
                it[createdAt] = now
            }
            inserted++
        }

        return inserted
    }

    private data class AreaSeed(
        val name: String,
        val lat: Double,
        val lng: Double
    )

    private data class AttractionSeed(
        val name: String,
        val areaName: String,
        val description: String,
        val highlights: String,
        val lat: Double,
        val lng: Double,
        val openHours: String?,
        val featured: Boolean
    )

    private data class PlanSeed(
        val name: String,
        val description: String,
        val price: BigDecimal,
        val period: BillingPeriod,
        val active: Boolean
    )

    private data class UserSeed(
        val fullName: String,
        val email: String,
        val createdAt: Instant
    )
}
