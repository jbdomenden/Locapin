package locapin.admin.repositories

import locapin.admin.models.AttractionPhotos
import locapin.admin.models.PhotoView
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime

class PhotoRepository {
    fun forAttraction(attractionId: Long): List<PhotoView> = AttractionPhotos.selectAll()
        .where { AttractionPhotos.attractionId eq attractionId }
        .orderBy(AttractionPhotos.sortOrder)
        .map { PhotoView(it[AttractionPhotos.id].value, it[AttractionPhotos.attractionId].value, it[AttractionPhotos.imagePath], it[AttractionPhotos.sortOrder]) }

    fun create(attractionId: Long, path: String, sortOrder: Int, now: LocalDateTime) {
        AttractionPhotos.insert {
            it[AttractionPhotos.attractionId] = attractionId
            it[imagePath] = path
            it[AttractionPhotos.sortOrder] = sortOrder
            it[createdAt] = now
        }
    }

    fun delete(id: Long) {
        AttractionPhotos.deleteWhere { AttractionPhotos.id eq id }
    }

    fun reorder(orders: Map<Long, Int>) {
        orders.forEach { (id, sort) ->
            AttractionPhotos.update({ AttractionPhotos.id eq id }) { it[sortOrder] = sort }
        }
    }
}
