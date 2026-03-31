package locapin.admin.repositories

import locapin.admin.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

data class AdminUserRecord(
    val id: Long,
    val fullName: String,
    val email: String,
    val passwordHash: String,
    val role: AdminRole,
    val status: AdminAccountStatus
)

data class PermissionRecord(
    val moduleKey: ModuleKey,
    val canCreate: Boolean,
    val canRead: Boolean,
    val canUpdate: Boolean,
    val canDelete: Boolean
)

class AdminRepository {
    private fun ResultRow.toRecord() = AdminUserRecord(
        this[AdminUsersTable.id],
        this[AdminUsersTable.fullName],
        this[AdminUsersTable.email],
        this[AdminUsersTable.passwordHash],
        this[AdminUsersTable.role],
        this[AdminUsersTable.status]
    )

    fun count(): Long = transaction { AdminUsersTable.selectAll().count() }

    fun countActiveSuperAdmins(): Long = transaction {
        AdminUsersTable.selectAll().where {
            (AdminUsersTable.role eq AdminRole.SUPER_ADMIN) and (AdminUsersTable.status eq AdminAccountStatus.ACTIVE)
        }.count()
    }

    fun listUsers(search: String?, role: AdminRole?, status: AdminAccountStatus?): List<Map<String, Any?>> = transaction {
        var q = AdminUsersTable.selectAll()
        if (!search.isNullOrBlank()) {
            val s = "%${search.trim()}%"
            q = q.where { (AdminUsersTable.fullName like s) or (AdminUsersTable.email like s) }
        }
        if (role != null) q = q.where { AdminUsersTable.role eq role }
        if (status != null) q = q.where { AdminUsersTable.status eq status }

        q.orderBy(AdminUsersTable.createdAt, SortOrder.DESC).map {
            mapOf(
                "id" to it[AdminUsersTable.id],
                "fullName" to it[AdminUsersTable.fullName],
                "email" to it[AdminUsersTable.email],
                "role" to it[AdminUsersTable.role].name,
                "status" to it[AdminUsersTable.status].name,
                "lastLoginAt" to it[AdminUsersTable.lastLoginAt]?.toString(),
                "createdAt" to it[AdminUsersTable.createdAt].toString()
            )
        }
    }

    fun findByEmail(email: String): AdminUserRecord? = transaction {
        AdminUsersTable.selectAll().where { AdminUsersTable.email eq email }.singleOrNull()?.toRecord()
    }

    fun findById(id: Long): AdminUserRecord? = transaction {
        AdminUsersTable.selectAll().where { AdminUsersTable.id eq id }.singleOrNull()?.toRecord()
    }

    fun create(
        fullName: String,
        email: String,
        passwordHash: String,
        role: AdminRole,
        status: AdminAccountStatus = AdminAccountStatus.ACTIVE,
        createdBy: Long? = null
    ): Long = transaction {
        AdminUsersTable.insert {
            it[AdminUsersTable.fullName] = fullName
            it[AdminUsersTable.email] = email
            it[AdminUsersTable.passwordHash] = passwordHash
            it[AdminUsersTable.role] = role
            it[AdminUsersTable.status] = status
            it[AdminUsersTable.createdBy] = createdBy
            it[createdAt] = Instant.now()
            it[updatedAt] = Instant.now()
        }[AdminUsersTable.id]
    }

    fun updateUser(id: Long, fullName: String, email: String, role: AdminRole, status: AdminAccountStatus): Boolean = transaction {
        AdminUsersTable.update({ AdminUsersTable.id eq id }) {
            it[AdminUsersTable.fullName] = fullName
            it[AdminUsersTable.email] = email
            it[AdminUsersTable.role] = role
            it[AdminUsersTable.status] = status
            it[updatedAt] = Instant.now()
        } > 0
    }

    fun deleteUser(id: Long): Boolean = transaction { AdminUsersTable.deleteWhere { AdminUsersTable.id eq id } > 0 }

    fun updatePassword(adminId: Long, passwordHash: String): Boolean = transaction {
        AdminUsersTable.update({ AdminUsersTable.id eq adminId }) {
            it[AdminUsersTable.passwordHash] = passwordHash
            it[updatedAt] = Instant.now()
        } > 0
    }

    fun touchLastLogin(adminId: Long) = transaction {
        AdminUsersTable.update({ AdminUsersTable.id eq adminId }) {
            it[lastLoginAt] = Instant.now()
            it[updatedAt] = Instant.now()
        }
    }

    fun getPermissions(adminId: Long): List<PermissionRecord> = transaction {
        AdminPermissionsTable.selectAll().where { AdminPermissionsTable.adminUserId eq adminId }.map {
            PermissionRecord(
                moduleKey = it[AdminPermissionsTable.moduleKey],
                canCreate = it[AdminPermissionsTable.canCreate],
                canRead = it[AdminPermissionsTable.canRead],
                canUpdate = it[AdminPermissionsTable.canUpdate],
                canDelete = it[AdminPermissionsTable.canDelete]
            )
        }
    }

    fun replacePermissions(adminId: Long, permissions: List<PermissionRecord>) = transaction {
        AdminPermissionsTable.deleteWhere { AdminPermissionsTable.adminUserId eq adminId }
        permissions.forEach { p ->
            AdminPermissionsTable.insert {
                it[adminUserId] = adminId
                it[moduleKey] = p.moduleKey
                it[canCreate] = p.canCreate
                it[canRead] = p.canRead
                it[canUpdate] = p.canUpdate
                it[canDelete] = p.canDelete
                it[createdAt] = Instant.now()
                it[updatedAt] = Instant.now()
            }
        }
    }

    fun defaultModeratorPermissions(): List<PermissionRecord> = ModuleKey.entries.map {
        val read = it != ModuleKey.USER_MANAGEMENT
        PermissionRecord(it, canCreate = false, canRead = read, canUpdate = false, canDelete = false)
    }
}
