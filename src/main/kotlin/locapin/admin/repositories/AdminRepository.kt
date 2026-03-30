package locapin.admin.repositories

import locapin.admin.models.AdminRole
import locapin.admin.models.AdminUsersTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

data class AdminUserRecord(val id: Long, val name: String, val email: String, val passwordHash: String, val role: AdminRole)

class AdminRepository {
    private fun ResultRow.toRecord() = AdminUserRecord(
        this[AdminUsersTable.id], this[AdminUsersTable.name], this[AdminUsersTable.email], this[AdminUsersTable.passwordHash], this[AdminUsersTable.role]
    )

    fun count(): Long = transaction { AdminUsersTable.selectAll().count() }

    fun findByEmail(email: String): AdminUserRecord? = transaction {
        AdminUsersTable.selectAll().where { AdminUsersTable.email eq email }.singleOrNull()?.toRecord()
    }

    fun findById(id: Long): AdminUserRecord? = transaction {
        AdminUsersTable.selectAll().where { AdminUsersTable.id eq id }.singleOrNull()?.toRecord()
    }

    fun create(name: String, email: String, passwordHash: String, role: AdminRole): Long = transaction {
        AdminUsersTable.insert {
            it[AdminUsersTable.name] = name
            it[AdminUsersTable.email] = email
            it[AdminUsersTable.passwordHash] = passwordHash
            it[AdminUsersTable.role] = role
            it[isActive] = true
            it[createdAt] = Instant.now()
            it[updatedAt] = Instant.now()
        }[AdminUsersTable.id]
    }

    fun updatePassword(adminId: Long, passwordHash: String): Boolean = transaction {
        AdminUsersTable.update({ AdminUsersTable.id eq adminId }) {
            it[AdminUsersTable.passwordHash] = passwordHash
            it[updatedAt] = Instant.now()
        } > 0
    }
}
