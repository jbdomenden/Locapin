package locapin.admin.repositories

import locapin.admin.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class AnalyticsRepository {
    fun dashboardStats(attractions: List<AttractionView>): DashboardStats {
        val totalCities = Cities.selectAll().count()
        val totalAreas = Areas.selectAll().count()
        val totalAttractions = Attractions.selectAll().count()
        val totalPhotos = AttractionPhotos.selectAll().count()
        val totalUsers = Users.selectAll().count()
        val totalPremiumSubscribers = Subscriptions.selectAll().where { Subscriptions.status eq SubscriptionStatus.ACTIVE }.count()
        val featured = Attractions.selectAll().where { Attractions.isFeatured eq true }.count()
        val active = Attractions.selectAll().where { Attractions.status eq RecordStatus.ACTIVE }.count()
        val inactive = totalAttractions - active

        return DashboardStats(totalCities, totalAreas, totalAttractions, totalPhotos, totalUsers, totalPremiumSubscribers, featured, active, inactive, attractions)
    }

    fun attractionsPerCity(): Map<String, Long> {
        val countExpr = Attractions.id.count()
        return (Attractions innerJoin Cities)
            .slice(Cities.name, countExpr)
            .selectAll()
            .groupBy(Cities.name)
            .associate { it[Cities.name] to it[countExpr] }
    }

    fun attractionsPerArea(): Map<String, Long> {
        val countExpr = Attractions.id.count()
        return (Attractions innerJoin Areas)
            .slice(Areas.name, countExpr)
            .selectAll()
            .groupBy(Areas.name)
            .associate { it[Areas.name] to it[countExpr] }
    }
}
