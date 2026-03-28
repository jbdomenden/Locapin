package locapin.admin.services

import locapin.admin.models.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.LocalDateTime

object SeedService {
    fun seedDefaultData() = transaction {
        val now = LocalDateTime.now()
        val seededCityId = if (Cities.selectAll().empty()) {
            Cities.insert {
                it[name] = "San Juan City"
                it[isPremium] = true
                it[status] = RecordStatus.ACTIVE
                it[createdAt] = now
                it[updatedAt] = now
            } get Cities.id
        } else Cities.selectAll().first()[Cities.id]

        if (Areas.selectAll().empty()) {
            listOf("Greenhills", "Little Baguio", "West Crame").forEachIndexed { idx, areaName ->
                Areas.insert {
                    it[Areas.cityId] = seededCityId
                    it[name] = areaName
                    it[centerLatitude] = 14.60 + (idx * 0.01)
                    it[centerLongitude] = 121.03 + (idx * 0.01)
                    it[status] = RecordStatus.ACTIVE
                    it[createdAt] = now
                    it[updatedAt] = now
                }
            }
        }

        if (SubscriptionPlans.selectAll().empty()) {
            SubscriptionPlans.insert {
                it[name] = "Monthly Premium"
                it[description] = "Premium access for 30 days"
                it[price] = BigDecimal("149.00")
                it[billingPeriod] = BillingPeriod.MONTHLY
                it[isActive] = true
                it[createdAt] = now
                it[updatedAt] = now
            }
            SubscriptionPlans.insert {
                it[name] = "Yearly Premium"
                it[description] = "Premium access for one year"
                it[price] = BigDecimal("1499.00")
                it[billingPeriod] = BillingPeriod.YEARLY
                it[isActive] = true
                it[createdAt] = now
                it[updatedAt] = now
            }
        }
    }
}
