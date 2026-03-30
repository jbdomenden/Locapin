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

    fun changePassword(adminId: Long, currentPassword: String, newPassword: String, confirmNewPassword: String): String? {
        if (newPassword.length < 8) return "New password must be at least 8 characters."
        if (newPassword != confirmNewPassword) return "New password and confirmation do not match."

        val admin = adminRepository.findById(adminId) ?: return "Admin account not found."
        if (!Passwords.verify(currentPassword, admin.passwordHash)) return "Current password is incorrect."

        val updated = adminRepository.updatePassword(adminId, Passwords.hash(newPassword))
        return if (updated) null else "Failed to update password."
    }
}
