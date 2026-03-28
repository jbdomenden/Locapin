package locapin.admin.repositories

import locapin.admin.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime

class AttractionRepository {
    private val baseJoin = Attractions.join(Cities, JoinType.INNER, Attractions.cityId, Cities.id)
        .join(Areas, JoinType.INNER, Attractions.areaId, Areas.id)

    fun all(cityId: Long?, areaId: Long?, search: String?): List<AttractionView> {
        val q = baseJoin.selectAll()
        if (cityId != null) q.andWhere { Attractions.cityId eq cityId }
        if (areaId != null) q.andWhere { Attractions.areaId eq areaId }
        if (!search.isNullOrBlank()) q.andWhere { Attractions.name like "%${search.trim()}%" }
        return q.orderBy(Attractions.createdAt, SortOrder.DESC).map { toView(it) }
    }

    fun latest(limit: Int): List<AttractionView> = baseJoin.selectAll().orderBy(Attractions.createdAt, SortOrder.DESC).limit(limit.toLong()).map { toView(it) }

    fun get(id: Long): AttractionView? = baseJoin.selectAll().where { Attractions.id eq id }.map { toView(it) }.singleOrNull()

    fun create(cityId: Long, areaId: Long, name: String, description: String, highlights: String, lat: Double, lng: Double, openHours: String?, status: RecordStatus, isFeatured: Boolean, now: LocalDateTime) {
        Attractions.insert {
            it[Attractions.cityId] = cityId
            it[Attractions.areaId] = areaId
            it[Attractions.name] = name
            it[Attractions.description] = description
            it[Attractions.highlights] = highlights
            it[latitude] = lat
            it[longitude] = lng
            it[Attractions.openHours] = openHours
            it[Attractions.status] = status
            it[Attractions.isFeatured] = isFeatured
            it[createdAt] = now
            it[updatedAt] = now
        }
    }

    fun update(id: Long, cityId: Long, areaId: Long, name: String, description: String, highlights: String, lat: Double, lng: Double, openHours: String?, status: RecordStatus, isFeatured: Boolean, now: LocalDateTime) {
        Attractions.update({ Attractions.id eq id }) {
            it[Attractions.cityId] = cityId
            it[Attractions.areaId] = areaId
            it[Attractions.name] = name
            it[Attractions.description] = description
            it[Attractions.highlights] = highlights
            it[latitude] = lat
            it[longitude] = lng
            it[Attractions.openHours] = openHours
            it[Attractions.status] = status
            it[Attractions.isFeatured] = isFeatured
            it[updatedAt] = now
        }
    }

    fun softDelete(id: Long, now: LocalDateTime) {
        Attractions.update({ Attractions.id eq id }) {
            it[status] = RecordStatus.ARCHIVED
            it[updatedAt] = now
        }
    }

    private fun toView(row: ResultRow) = AttractionView(
        id = row[Attractions.id].value,
        cityId = row[Attractions.cityId].value,
        cityName = row[Cities.name],
        areaId = row[Attractions.areaId].value,
        areaName = row[Areas.name],
        name = row[Attractions.name],
        description = row[Attractions.description],
        highlights = row[Attractions.highlights],
        latitude = row[Attractions.latitude],
        longitude = row[Attractions.longitude],
        openHours = row[Attractions.openHours],
        status = row[Attractions.status],
        isFeatured = row[Attractions.isFeatured],
        createdAt = row[Attractions.createdAt]
    )
}
