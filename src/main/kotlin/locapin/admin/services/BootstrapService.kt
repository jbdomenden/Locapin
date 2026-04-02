package locapin.admin.services

import locapin.admin.config.AppConfig
import locapin.admin.models.*
import locapin.admin.repositories.AdminRepository
import locapin.admin.repositories.PermissionRecord
import locapin.admin.utils.Passwords
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

        logger.info("Bootstrap superadmin credentials -> user: {} | pass: {}", superAdminEmail, superAdminPassword)

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

        val cityIds = if (CitiesTable.selectAll().count() == 0L) {
            mapOf(
                "San Juan City" to CitiesTable.insert {
                    it[name] = "San Juan City"
                    it[isPremium] = true
                    it[status] = EntityStatus.ACTIVE
                    it[createdAt] = now
                    it[updatedAt] = now
                }[CitiesTable.id],
                "Mandaluyong City" to CitiesTable.insert {
                    it[name] = "Mandaluyong City"
                    it[isPremium] = false
                    it[status] = EntityStatus.ACTIVE
                    it[createdAt] = now
                    it[updatedAt] = now
                }[CitiesTable.id],
                "Quezon City" to CitiesTable.insert {
                    it[name] = "Quezon City"
                    it[isPremium] = true
                    it[status] = EntityStatus.ACTIVE
                    it[createdAt] = now
                    it[updatedAt] = now
                }[CitiesTable.id]
            )
        } else {
            CitiesTable.selectAll().associate { it[CitiesTable.name] to it[CitiesTable.id] }
        }

        val areaIds = if (AreasTable.selectAll().count() == 0L) {
            mapOf(
                "Greenhills" to insertArea(cityIds.getValue("San Juan City"), "Greenhills", 14.6038, 121.0496, now),
                "Pinaglabanan" to insertArea(cityIds.getValue("San Juan City"), "Pinaglabanan", 14.6018, 121.0319, now),
                "Ortigas Center" to insertArea(cityIds.getValue("Mandaluyong City"), "Ortigas Center", 14.5868, 121.0564, now),
                "Cubao" to insertArea(cityIds.getValue("Quezon City"), "Cubao", 14.6195, 121.0533, now)
            )
        } else {
            AreasTable.selectAll().associate { it[AreasTable.name] to it[AreasTable.id] }
        }

        val attractionIds = if (AttractionsTable.selectAll().count() == 0L) {
            mapOf(
                "Museo ng Katipunan" to insertAttraction(cityIds.getValue("San Juan City"), areaIds.getValue("Pinaglabanan"), "Museo ng Katipunan", "A compact museum showcasing Katipunan memorabilia, local heritage archives, and guided exhibits.", "Historical artifacts, guided tours, educational displays", 14.6022, 121.0327, "Tue-Sun 9:00 AM - 5:00 PM", true, now),
                "Pinaglabanan Shrine" to insertAttraction(cityIds.getValue("San Juan City"), areaIds.getValue("Pinaglabanan"), "Pinaglabanan Shrine", "A key historical landmark that commemorates the Battle of San Juan del Monte.", "Heritage park, battle monument, open grounds", 14.6015, 121.0315, "Daily 6:00 AM - 8:00 PM", true, now),
                "Ronac Art Center" to insertAttraction(cityIds.getValue("San Juan City"), areaIds.getValue("Greenhills"), "Ronac Art Center", "A contemporary venue hosting rotating exhibitions from local and international artists.", "Modern exhibits, curated collections, art talks", 14.6048, 121.0523, "Wed-Mon 10:00 AM - 7:00 PM", false, now),
                "Fundacion Sanso" to insertAttraction(cityIds.getValue("San Juan City"), areaIds.getValue("Greenhills"), "Fundacion Sanso", "Home of notable Philippine modern art pieces with regular curated exhibits.", "Museum quality galleries, artist archives, workshops", 14.5995, 121.0483, "Tue-Sun 10:00 AM - 6:00 PM", true, now),
                "Art Sector Gallery" to insertAttraction(cityIds.getValue("San Juan City"), areaIds.getValue("Greenhills"), "Art Sector Gallery", "A design-forward gallery spotlighting modern Filipino visual artists.", "Contemporary artwork, weekend events, artist meetups", 14.6032, 121.0487, "Thu-Tue 11:00 AM - 7:00 PM", false, now),
                "Greenhills Shopping Center" to insertAttraction(cityIds.getValue("San Juan City"), areaIds.getValue("Greenhills"), "Greenhills Shopping Center", "A landmark shopping district known for dining, retail, and lifestyle stores.", "Shopping complex, food choices, local finds", 14.6029, 121.0498, "Daily 10:00 AM - 9:00 PM", true, now),
                "Greenhills Promenade" to insertAttraction(cityIds.getValue("San Juan City"), areaIds.getValue("Greenhills"), "Greenhills Promenade", "Open-air promenade zone with entertainment, cinema, and cafes.", "Al fresco spaces, cinema, coffee spots", 14.6037, 121.0509, "Daily 10:00 AM - 10:00 PM", false, now),
                "V-Mall" to insertAttraction(cityIds.getValue("San Juan City"), areaIds.getValue("Greenhills"), "V-Mall", "A vibrant mall wing popular for gadgets, fashion boutiques, and specialty stores.", "Electronics stores, fashion stalls, weekend foot traffic", 14.6033, 121.0501, "Daily 10:00 AM - 9:00 PM", true, now)
            )
        } else {
            AttractionsTable.selectAll().associate { it[AttractionsTable.name] to it[AttractionsTable.id] }
        }

        val planIds = if (SubscriptionPlansTable.selectAll().count() == 0L) {
            mapOf(
                "Explorer" to insertPlan("Explorer", "Entry plan for casual travelers with curated city guides and attraction updates.", BigDecimal("199.00"), BillingPeriod.MONTHLY, true, now),
                "Premium" to insertPlan("Premium", "Enhanced traveler plan with premium city access and priority in-app support.", BigDecimal("499.00"), BillingPeriod.MONTHLY, true, now),
                "Annual Premium" to insertPlan("Annual Premium", "Annual savings plan for frequent travelers with full premium benefits.", BigDecimal("4990.00"), BillingPeriod.YEARLY, true, now)
            )
        } else {
            SubscriptionPlansTable.selectAll().associate { it[SubscriptionPlansTable.name] to it[SubscriptionPlansTable.id] }
        }

        val userIds = if (UsersTable.selectAll().count() == 0L) {
            mapOf(
                "Andrea Dela Cruz" to insertUser("Andrea Dela Cruz", "andrea.delacruz@example.com", now.minus(7, ChronoUnit.DAYS)),
                "Miguel Tan" to insertUser("Miguel Tan", "miguel.tan@example.com", now.minus(5, ChronoUnit.DAYS)),
                "Jasmine Flores" to insertUser("Jasmine Flores", "jasmine.flores@example.com", now.minus(2, ChronoUnit.DAYS))
            )
        } else {
            UsersTable.selectAll().associate { it[UsersTable.fullName] to it[UsersTable.id] }
        }

        if (SubscriptionsTable.selectAll().count() == 0L && userIds.isNotEmpty() && planIds.isNotEmpty()) {
            SubscriptionsTable.insert {
                it[userId] = userIds.getValue("Andrea Dela Cruz")
                it[planId] = planIds.getValue("Premium")
                it[isActive] = true
                it[startedAt] = now.minus(25, ChronoUnit.DAYS)
                it[expiresAt] = now.plus(5, ChronoUnit.DAYS)
            }
            SubscriptionsTable.insert {
                it[userId] = userIds.getValue("Miguel Tan")
                it[planId] = planIds.getValue("Annual Premium")
                it[isActive] = true
                it[startedAt] = now.minus(60, ChronoUnit.DAYS)
                it[expiresAt] = now.plus(305, ChronoUnit.DAYS)
            }
        }

        seedAttractionPhotosIfAssetsExist(attractionIds, now)
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

    private fun insertUser(fullName: String, email: String, createdAt: Instant): Long =
        UsersTable.insert {
            it[UsersTable.fullName] = fullName
            it[UsersTable.email] = email
            it[UsersTable.createdAt] = createdAt
        }[UsersTable.id]

    private fun seedAttractionPhotosIfAssetsExist(attractionIds: Map<String, Long>, now: Instant) {
        if (AttractionPhotosTable.selectAll().count() > 0L) return

        val candidatePath = "src/main/resources/static/images/locapin-logo.jpg"
        val photoAsset = java.io.File(candidatePath)
        if (!photoAsset.exists()) {
            logger.warn("Skipping attraction photo bootstrap because no local seed image asset exists.")
            return
        }

        val targetAttractions = listOf(
            "Museo ng Katipunan",
            "Pinaglabanan Shrine",
            "Greenhills Shopping Center",
            "V-Mall"
        )

        targetAttractions.forEachIndexed { index, attractionName ->
            val attractionId = attractionIds[attractionName] ?: return@forEachIndexed
            AttractionPhotosTable.insert {
                it[AttractionPhotosTable.attractionId] = attractionId
                it[imagePath] = "/static/images/locapin-logo.jpg"
                it[sortOrder] = index
                it[createdAt] = now
            }
        }
    }
}
