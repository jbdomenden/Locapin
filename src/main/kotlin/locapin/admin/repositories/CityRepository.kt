package locapin.admin.repositories

import locapin.admin.models.Cities
import locapin.admin.models.CityView
import locapin.admin.models.RecordStatus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime

class CityRepository {
    fun all(): List<CityView> = Cities.selectAll().orderBy(Cities.name).map {
        CityView(it[Cities.id].value, it[Cities.name], it[Cities.isPremium], it[Cities.status], it[Cities.createdAt])
    }

    fun create(name: String, isPremium: Boolean, status: RecordStatus, now: LocalDateTime) {
        Cities.insert {
            it[Cities.name] = name
            it[Cities.isPremium] = isPremium
            it[Cities.status] = status
            it[createdAt] = now
            it[updatedAt] = now
        }
    }

    fun get(id: Long): CityView? = Cities.selectAll().where { Cities.id eq id }.map {
        CityView(it[Cities.id].value, it[Cities.name], it[Cities.isPremium], it[Cities.status], it[Cities.createdAt])
    }.singleOrNull()

    fun update(id: Long, name: String, isPremium: Boolean, status: RecordStatus, now: LocalDateTime) {
        Cities.update({ Cities.id eq id }) {
            it[Cities.name] = name
            it[Cities.isPremium] = isPremium
            it[Cities.status] = status
            it[updatedAt] = now
        }
    }
}
