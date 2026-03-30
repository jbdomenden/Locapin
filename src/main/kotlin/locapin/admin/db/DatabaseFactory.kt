package locapin.admin.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import locapin.admin.config.AppConfig
import locapin.admin.models.*
import locapin.admin.repositories.AdminRepository
import locapin.admin.services.SeedService
import locapin.admin.utils.Passwords
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.io.File
import java.time.LocalDateTime

object DatabaseFactory {
    fun init(config: AppConfig) {
        val hikari = HikariConfig().apply {
            jdbcUrl = config.dbUrl
            username = config.dbUser
            password = config.dbPassword
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        val ds = HikariDataSource(hikari)
        Database.connect(ds)
        TransactionManager.defaultDatabase

        transaction {
            SchemaUtils.create(
                AdminUsers, Cities, Areas, Attractions, AttractionPhotos,
                Users, SubscriptionPlans, Subscriptions
            )
        }

        File(config.fileUploadDir).mkdirs()
    }

    fun seed(config: AppConfig) {
        transaction {
            val adminRepo = AdminRepository()
            if (!adminRepo.anyAdminExists()) {
                adminRepo.create(
                    fullName = config.adminInitialName,
                    email = config.adminInitialEmail,
                    passwordHash = Passwords.hash(config.adminInitialPassword),
                    role = AdminRole.SUPER_ADMIN,
                    status = RecordStatus.ACTIVE,
                    now = LocalDateTime.now()
                )
            }
        }
        SeedService.seedDefaultData()
    }
}
