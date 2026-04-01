package locapin.admin.services

import locapin.admin.config.AppConfig
import locapin.admin.models.*
import locapin.admin.repositories.AdminRepository
import locapin.admin.utils.Passwords
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.Instant

class BootstrapService(private val config: AppConfig) {
    private val adminRepository = AdminRepository()
    private val logger = LoggerFactory.getLogger(BootstrapService::class.java)

    fun bootstrap() {
        val superAdminEmail = config.adminInitialEmail.lowercase()
        val superAdminPassword = config.adminInitialPassword

        logger.info(
            "Bootstrap superadmin credentials -> user: {} | pass: {}",
            superAdminEmail,
            superAdminPassword
        )

        if (adminRepository.count() == 0L) {
            adminRepository.create(
                fullName = config.adminInitialName,
                email = superAdminEmail,
                passwordHash = Passwords.hash(superAdminPassword),
                role = AdminRole.SUPER_ADMIN
            )
            logger.info("Bootstrapped initial superadmin account.")
        }
        transaction {
            if (CitiesTable.selectAll().count() == 0L) {
                val cityId = CitiesTable.insert {
                    it[name] = "San Juan City"; it[isPremium] = true; it[status] = EntityStatus.ACTIVE; it[createdAt] = Instant.now(); it[updatedAt] = Instant.now()
                }[CitiesTable.id]
                AreasTable.insert {
                    it[AreasTable.cityId] = cityId; it[name] = "Poblacion"; it[centerLatitude] = 14.6028; it[centerLongitude] = 121.0354; it[status]=EntityStatus.ACTIVE; it[createdAt]=Instant.now(); it[updatedAt]=Instant.now()
                }
            }
            if (SubscriptionPlansTable.selectAll().count() == 0L) {
                SubscriptionPlansTable.insert {
                    it[name] = "Explorer"; it[description] = "Monthly premium tourism access"; it[price] = BigDecimal("99.00"); it[billingPeriod]=BillingPeriod.MONTHLY; it[isActive]=true; it[createdAt]=Instant.now(); it[updatedAt]=Instant.now()
                }
            }
        }
    }
}
