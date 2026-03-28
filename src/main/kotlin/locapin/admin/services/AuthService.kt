package locapin.admin.services

import locapin.admin.models.AdminUser
import locapin.admin.repositories.AdminRepository
import locapin.admin.utils.Passwords
import org.jetbrains.exposed.sql.transactions.transaction

class AuthService(private val repo: AdminRepository = AdminRepository()) {
    fun authenticate(email: String, password: String): AdminUser? = transaction {
        val user = repo.findByEmail(email) ?: return@transaction null
        if (!Passwords.verify(password, user.passwordHash)) return@transaction null
        user
    }
}
