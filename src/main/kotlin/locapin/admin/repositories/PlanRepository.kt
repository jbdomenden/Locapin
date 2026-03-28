package locapin.admin.repositories

import locapin.admin.models.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.math.BigDecimal
import java.time.LocalDateTime

class PlanRepository {
    fun all(): List<PlanView> = SubscriptionPlans.selectAll().orderBy(SubscriptionPlans.name).map {
        PlanView(it[SubscriptionPlans.id].value, it[SubscriptionPlans.name], it[SubscriptionPlans.description], it[SubscriptionPlans.price], it[SubscriptionPlans.billingPeriod], it[SubscriptionPlans.isActive])
    }

    fun get(id: Long): PlanView? = SubscriptionPlans.selectAll().where { SubscriptionPlans.id eq id }.map {
        PlanView(it[SubscriptionPlans.id].value, it[SubscriptionPlans.name], it[SubscriptionPlans.description], it[SubscriptionPlans.price], it[SubscriptionPlans.billingPeriod], it[SubscriptionPlans.isActive])
    }.singleOrNull()

    fun create(name: String, desc: String, price: BigDecimal, period: BillingPeriod, isActive: Boolean, now: LocalDateTime) {
        SubscriptionPlans.insert {
            it[SubscriptionPlans.name] = name
            it[description] = desc
            it[SubscriptionPlans.price] = price
            it[billingPeriod] = period
            it[SubscriptionPlans.isActive] = isActive
            it[createdAt] = now
            it[updatedAt] = now
        }
    }

    fun update(id: Long, name: String, desc: String, price: BigDecimal, period: BillingPeriod, isActive: Boolean, now: LocalDateTime) {
        SubscriptionPlans.update({ SubscriptionPlans.id eq id }) {
            it[SubscriptionPlans.name] = name
            it[description] = desc
            it[SubscriptionPlans.price] = price
            it[billingPeriod] = period
            it[SubscriptionPlans.isActive] = isActive
            it[updatedAt] = now
        }
    }
}
