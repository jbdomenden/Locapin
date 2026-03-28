package locapin.admin.repositories

import locapin.admin.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime

class AreaRepository {
    fun all(cityId: Long?): List<AreaView> {
        val q = (Areas innerJoin Cities).selectAll()
        if (cityId != null) q.andWhere { Areas.cityId eq cityId }
        return q.orderBy(Areas.name).map {
            AreaView(
                it[Areas.id].value,
                it[Areas.cityId].value,
                it[Cities.name],
                it[Areas.name],
                it[Areas.centerLatitude],
                it[Areas.centerLongitude],
                it[Areas.status]
            )
        }
    }

    fun byCity(cityId: Long): List<AreaView> = all(cityId)

    fun get(id: Long): AreaView? = (Areas innerJoin Cities).selectAll().where { Areas.id eq id }.map {
        AreaView(
            it[Areas.id].value,
            it[Areas.cityId].value,
            it[Cities.name],
            it[Areas.name],
            it[Areas.centerLatitude],
            it[Areas.centerLongitude],
            it[Areas.status]
        )
    }.singleOrNull()

    fun create(cityId: Long, name: String, lat: Double, lng: Double, boundary: String?, status: RecordStatus, now: LocalDateTime) {
        Areas.insert {
            it[Areas.cityId] = cityId
            it[Areas.name] = name
            it[centerLatitude] = lat
            it[centerLongitude] = lng
            it[boundaryData] = boundary
            it[Areas.status] = status
            it[createdAt] = now
            it[updatedAt] = now
        }
    }

    fun update(id: Long, cityId: Long, name: String, lat: Double, lng: Double, boundary: String?, status: RecordStatus, now: LocalDateTime) {
        Areas.update({ Areas.id eq id }) {
            it[Areas.cityId] = cityId
            it[Areas.name] = name
            it[centerLatitude] = lat
            it[centerLongitude] = lng
            it[boundaryData] = boundary
            it[Areas.status] = status
            it[updatedAt] = now
        }
    }
}
