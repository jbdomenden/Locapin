package locapin.admin.services

import locapin.admin.models.AdminSessionUser
import locapin.admin.repositories.AdminRepository
import locapin.admin.utils.Passwords

class AuthService(private val adminRepository: AdminRepository = AdminRepository()) {
    fun login(email: String, password: String): AdminSessionUser? {
        val admin = adminRepository.findByEmail(email.trim().lowercase()) ?: return null
        if (!Passwords.verify(password, admin.passwordHash)) return null
        return AdminSessionUser(admin.id, admin.name, admin.email, admin.role)
    }
}
