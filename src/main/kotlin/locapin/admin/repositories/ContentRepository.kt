package locapin.admin.repositories

import io.ktor.server.plugins.BadRequestException
import locapin.admin.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.Instant

class ContentRepository {
    fun dashboardStats(): Map<String, Any> = transaction {
        mapOf(
            "totalAreas" to AreasTable.selectAll().count(),
            "totalAttractions" to AttractionsTable.selectAll().where { AttractionsTable.isDeleted eq false }.count(),
            "totalPhotos" to AttractionPhotosTable.selectAll().count(),
            "totalUsers" to UsersTable.selectAll().count(),
            "totalPremiumSubscribers" to SubscriptionsTable.selectAll().where { SubscriptionsTable.isActive eq true }.count(),
            "featuredAttractions" to AttractionsTable.selectAll().where { AttractionsTable.isFeatured eq true and (AttractionsTable.isDeleted eq false) }.count(),
            "activeAttractions" to AttractionsTable.selectAll().where { AttractionsTable.status eq EntityStatus.ACTIVE and (AttractionsTable.isDeleted eq false) }.count(),
            "inactiveAttractions" to AttractionsTable.selectAll().where { AttractionsTable.status neq EntityStatus.ACTIVE and (AttractionsTable.isDeleted eq false) }.count(),
            "latestAttractions" to AttractionsTable.selectAll().where { AttractionsTable.isDeleted eq false }
                .orderBy(AttractionsTable.createdAt, SortOrder.DESC).limit(5)
                .map { mapOf("id" to it[AttractionsTable.id], "name" to it[AttractionsTable.name]) },
            "newestUsers" to UsersTable.selectAll().orderBy(UsersTable.createdAt, SortOrder.DESC).limit(5)
                .map { mapOf("id" to it[UsersTable.id], "name" to it[UsersTable.fullName], "email" to it[UsersTable.email]) }
        )
    }

    fun listAreas() = transaction {
        AreasTable.selectAll().where { AreasTable.cityId eq resolveSanJuanCityId() }
            .orderBy(AreasTable.name)
            .map { ResultRowSerializer.area(it) }
    }

    fun getArea(id: Long) = transaction {
        AreasTable.selectAll().where { (AreasTable.id eq id) and (AreasTable.cityId eq resolveSanJuanCityId()) }
            .singleOrNull()?.let { ResultRowSerializer.area(it) }
    }

    fun createArea(req: AreaRequest) = transaction {
        AreasTable.insert {
            it[cityId] = resolveSanJuanCityId()
            it[name] = req.name.trim()
            it[centerLatitude] = req.centerLatitude
            it[centerLongitude] = req.centerLongitude
            it[boundaryData] = req.boundaryData
            it[status] = EntityStatus.ACTIVE
            it[createdAt] = Instant.now()
            it[updatedAt] = Instant.now()
        }[AreasTable.id]
    }

    fun updateArea(id: Long, req: AreaRequest) = transaction {
        AreasTable.update({ (AreasTable.id eq id) and (AreasTable.cityId eq resolveSanJuanCityId()) }) {
            it[cityId] = resolveSanJuanCityId()
            it[name] = req.name.trim()
            it[centerLatitude] = req.centerLatitude
            it[centerLongitude] = req.centerLongitude
            it[boundaryData] = req.boundaryData
            it[updatedAt] = Instant.now()
        }
    }

    fun updateAreaStatus(id: Long, status: EntityStatus) = transaction {
        AreasTable.update({ (AreasTable.id eq id) and (AreasTable.cityId eq resolveSanJuanCityId()) }) {
            it[AreasTable.status] = status
            it[updatedAt] = Instant.now()
        }
    }

    fun listAttractions(areaId: Long?, q: String?) = transaction {
        var query = AttractionsTable.selectAll().where {
            (AttractionsTable.isDeleted eq false) and (AttractionsTable.cityId eq resolveSanJuanCityId())
        }
        if (areaId != null) query = query.where { AttractionsTable.areaId eq areaId }
        if (!q.isNullOrBlank()) query = query.where { AttractionsTable.name like "%${q.trim()}%" }
        query.orderBy(AttractionsTable.createdAt, SortOrder.DESC).map { ResultRowSerializer.attraction(it) }
    }

    fun getAttraction(id: Long) = transaction {
        AttractionsTable.selectAll().where {
            (AttractionsTable.id eq id) and
                (AttractionsTable.isDeleted eq false) and
                (AttractionsTable.cityId eq resolveSanJuanCityId())
        }.singleOrNull()?.let { ResultRowSerializer.attraction(it) }
    }

    fun createAttraction(req: AttractionRequest) = transaction {
        AttractionsTable.insert {
            it[cityId] = resolveSanJuanCityId()
            it[areaId] = req.areaId
            it[name] = req.name.trim()
            it[description] = req.description.trim()
            it[highlights] = req.highlights.trim()
            it[latitude] = req.latitude
            it[longitude] = req.longitude
            it[openHours] = req.openHours
            it[status] = EntityStatus.ACTIVE
            it[isFeatured] = false
            it[isDeleted] = false
            it[createdAt] = Instant.now()
            it[updatedAt] = Instant.now()
        }[AttractionsTable.id]
    }

    fun updateAttraction(id: Long, req: AttractionRequest) = transaction {
        AttractionsTable.update({ (AttractionsTable.id eq id) and (AttractionsTable.cityId eq resolveSanJuanCityId()) }) {
            it[cityId] = resolveSanJuanCityId()
            it[areaId] = req.areaId
            it[name] = req.name.trim()
            it[description] = req.description.trim()
            it[highlights] = req.highlights.trim()
            it[latitude] = req.latitude
            it[longitude] = req.longitude
            it[openHours] = req.openHours
            it[updatedAt] = Instant.now()
        }
    }

    fun updateAttractionStatus(id: Long, status: EntityStatus) = transaction {
        AttractionsTable.update({ (AttractionsTable.id eq id) and (AttractionsTable.cityId eq resolveSanJuanCityId()) }) {
            it[AttractionsTable.status] = status
            it[updatedAt] = Instant.now()
        }
    }

    fun updateAttractionFeatured(id: Long, isFeatured: Boolean) = transaction {
        AttractionsTable.update({ (AttractionsTable.id eq id) and (AttractionsTable.cityId eq resolveSanJuanCityId()) }) {
            it[AttractionsTable.isFeatured] = isFeatured
            it[updatedAt] = Instant.now()
        }
    }

    fun softDeleteAttraction(id: Long) = transaction {
        AttractionsTable.update({ (AttractionsTable.id eq id) and (AttractionsTable.cityId eq resolveSanJuanCityId()) }) {
            it[isDeleted] = true
            it[updatedAt] = Instant.now()
        }
    }

    fun listPlans() = transaction { SubscriptionPlansTable.selectAll().orderBy(SubscriptionPlansTable.createdAt, SortOrder.DESC).map { ResultRowSerializer.plan(it) } }
    fun getPlan(id: Long) = transaction { SubscriptionPlansTable.selectAll().where { SubscriptionPlansTable.id eq id }.singleOrNull()?.let { ResultRowSerializer.plan(it) } }
    fun createPlan(req: PlanRequest) = transaction { SubscriptionPlansTable.insert { it[name]=req.name.trim();it[description]=req.description.trim();it[price]=BigDecimal.valueOf(req.price);it[billingPeriod]=req.billingPeriod;it[isActive]=req.isActive;it[createdAt]=Instant.now();it[updatedAt]=Instant.now() }[SubscriptionPlansTable.id] }
    fun updatePlan(id: Long, req: PlanRequest) = transaction { SubscriptionPlansTable.update({ SubscriptionPlansTable.id eq id }) { it[name]=req.name.trim();it[description]=req.description.trim();it[price]=BigDecimal.valueOf(req.price);it[billingPeriod]=req.billingPeriod;it[isActive]=req.isActive;it[updatedAt]=Instant.now() } }
    fun updatePlanStatus(id: Long, active: Boolean) = transaction { SubscriptionPlansTable.update({ SubscriptionPlansTable.id eq id }) { it[isActive]=active;it[updatedAt]=Instant.now() } }

    fun listPhotos(attractionId: Long) = transaction { AttractionPhotosTable.selectAll().where { AttractionPhotosTable.attractionId eq attractionId }.orderBy(AttractionPhotosTable.sortOrder).map { ResultRowSerializer.photo(it) } }
    fun addPhoto(attractionId: Long, path: String, sortOrder: Int) = transaction { AttractionPhotosTable.insert { it[AttractionPhotosTable.attractionId]=attractionId;it[imagePath]=path;it[AttractionPhotosTable.sortOrder]=sortOrder;it[createdAt]=Instant.now() }[AttractionPhotosTable.id] }
    fun reorderPhotos(items: List<PhotoReorderItem>) = transaction { items.forEach { item -> AttractionPhotosTable.update({ AttractionPhotosTable.id eq item.id }) { it[sortOrder]=item.sortOrder } } }
    fun deletePhoto(id: Long) = transaction { AttractionPhotosTable.deleteWhere { AttractionPhotosTable.id eq id } }

    private fun Transaction.resolveSanJuanCityId(): Long {
        return CitiesTable.select(CitiesTable.id).where { CitiesTable.name eq "San Juan City" }.singleOrNull()?.get(CitiesTable.id)
            ?: CitiesTable.select(CitiesTable.id).limit(1).singleOrNull()?.get(CitiesTable.id)
            ?: throw BadRequestException("San Juan City is not configured")
    }
}

private object ResultRowSerializer {
    fun area(it: ResultRow) = mapOf("id" to it[AreasTable.id], "name" to it[AreasTable.name], "centerLatitude" to it[AreasTable.centerLatitude], "centerLongitude" to it[AreasTable.centerLongitude], "boundaryData" to it[AreasTable.boundaryData], "status" to it[AreasTable.status].name, "createdAt" to it[AreasTable.createdAt].toString(), "updatedAt" to it[AreasTable.updatedAt].toString())
    fun attraction(it: ResultRow) = mapOf("id" to it[AttractionsTable.id], "areaId" to it[AttractionsTable.areaId], "name" to it[AttractionsTable.name], "description" to it[AttractionsTable.description], "highlights" to it[AttractionsTable.highlights], "latitude" to it[AttractionsTable.latitude], "longitude" to it[AttractionsTable.longitude], "openHours" to it[AttractionsTable.openHours], "status" to it[AttractionsTable.status].name, "isFeatured" to it[AttractionsTable.isFeatured], "createdAt" to it[AttractionsTable.createdAt].toString(), "updatedAt" to it[AttractionsTable.updatedAt].toString())
    fun plan(it: ResultRow) = mapOf("id" to it[SubscriptionPlansTable.id], "name" to it[SubscriptionPlansTable.name], "description" to it[SubscriptionPlansTable.description], "price" to it[SubscriptionPlansTable.price].toDouble(), "billingPeriod" to it[SubscriptionPlansTable.billingPeriod].name, "isActive" to it[SubscriptionPlansTable.isActive], "createdAt" to it[SubscriptionPlansTable.createdAt].toString(), "updatedAt" to it[SubscriptionPlansTable.updatedAt].toString())
    fun photo(it: ResultRow) = mapOf("id" to it[AttractionPhotosTable.id], "attractionId" to it[AttractionPhotosTable.attractionId], "imagePath" to it[AttractionPhotosTable.imagePath], "sortOrder" to it[AttractionPhotosTable.sortOrder], "createdAt" to it[AttractionPhotosTable.createdAt].toString())
}
