package locapin.admin.repositories

import locapin.admin.models.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.time.LocalDateTime

class AdminRepository {
    fun anyAdminExists(): Boolean = AdminUsers.selectAll().limit(1).any()

    fun create(fullName: String, email: String, passwordHash: String, role: AdminRole, status: RecordStatus, now: LocalDateTime) {
        AdminUsers.insert {
            it[AdminUsers.fullName] = fullName
            it[AdminUsers.email] = email.lowercase()
            it[AdminUsers.passwordHash] = passwordHash
            it[AdminUsers.role] = role
            it[AdminUsers.status] = status
            it[createdAt] = now
            it[updatedAt] = now
        }
    }

    fun findByEmail(email: String): AdminUser? = AdminUsers.selectAll().where { AdminUsers.email eq email.lowercase() }
        .map {
            AdminUser(
                id = it[AdminUsers.id].value,
                fullName = it[AdminUsers.fullName],
                email = it[AdminUsers.email],
                passwordHash = it[AdminUsers.passwordHash],
                role = it[AdminUsers.role],
                status = it[AdminUsers.status]
            )
        }.singleOrNull()
}
