package locapin.admin.services

import io.ktor.server.plugins.BadRequestException
import locapin.admin.models.*
import locapin.admin.repositories.AdminRepository
import locapin.admin.repositories.ContentRepository
import locapin.admin.utils.Validators
import locapin.admin.utils.Passwords

class AdminService(
    private val repo: ContentRepository = ContentRepository(),
    private val adminRepo: AdminRepository = AdminRepository()
) {
    fun dashboard() = repo.dashboardStats()

    fun listAreas() = repo.listAreas()
    fun getArea(id: Long) = repo.getArea(id)
    fun createArea(req: AreaRequest): Long {
        Validators.requireNotBlank(req.name, "Area name")
        Validators.validateLatLng(req.centerLatitude, req.centerLongitude)
        return repo.createArea(req)
    }
    fun updateArea(id: Long, req: AreaRequest) {
        Validators.requireNotBlank(req.name, "Area name")
        Validators.validateLatLng(req.centerLatitude, req.centerLongitude)
        repo.updateArea(id, req)
    }
    fun updateAreaStatus(id: Long, status: EntityStatus) = repo.updateAreaStatus(id, status)

    fun listAttractions(areaId: Long?, q: String?) = repo.listAttractions(areaId, q)
    fun getAttraction(id: Long) = repo.getAttraction(id)
    fun createAttraction(req: AttractionRequest): Long {
        Validators.requireNotBlank(req.name, "Attraction name"); Validators.requireNotBlank(req.description, "Description"); Validators.requireNotBlank(req.highlights, "Highlights"); Validators.validateLatLng(req.latitude, req.longitude)
        repo.getArea(req.areaId) ?: throw BadRequestException("Area not found")
        return repo.createAttraction(req)
    }
    fun updateAttraction(id: Long, req: AttractionRequest) {
        Validators.requireNotBlank(req.name, "Attraction name"); Validators.requireNotBlank(req.description, "Description"); Validators.requireNotBlank(req.highlights, "Highlights"); Validators.validateLatLng(req.latitude, req.longitude)
        repo.getArea(req.areaId) ?: throw BadRequestException("Area not found")
        repo.updateAttraction(id, req)
    }
    fun updateAttractionStatus(id: Long, status: EntityStatus) = repo.updateAttractionStatus(id, status)
    fun updateAttractionFeatured(id: Long, featured: Boolean) = repo.updateAttractionFeatured(id, featured)
    fun deleteAttraction(id: Long) = repo.softDeleteAttraction(id)

    fun listPlans() = repo.listPlans()
    fun getPlan(id: Long) = repo.getPlan(id)
    fun createPlan(req: PlanRequest): Long { Validators.requireNotBlank(req.name, "Plan name"); Validators.requireNotBlank(req.description, "Plan description"); Validators.validatePrice(req.price); return repo.createPlan(req) }
    fun updatePlan(id: Long, req: PlanRequest) { Validators.validatePrice(req.price); repo.updatePlan(id, req) }
    fun updatePlanStatus(id: Long, active: Boolean) = repo.updatePlanStatus(id, active)

    fun listPhotos(attractionId: Long) = repo.listPhotos(attractionId)
    fun addPhoto(attractionId: Long, path: String, sortOrder: Int) = repo.addPhoto(attractionId, path, sortOrder)
    fun reorderPhotos(items: List<PhotoReorderItem>) {
        if (items.map { it.sortOrder }.any { it < 0 }) throw BadRequestException("Sort order must be non-negative")
        repo.reorderPhotos(items)
    }
    fun deletePhoto(id: Long) = repo.deletePhoto(id)

    fun listAdminUsers(search: String?, role: AdminRole?, status: AdminAccountStatus?) = adminRepo.listUsers(search, role, status)
    fun getAdminUser(id: Long) = adminRepo.findById(id)

    fun createAdminUser(actor: Long, req: AdminUserCreateRequest): Long {
        Validators.requireNotBlank(req.fullName, "Full name")
        Validators.requireNotBlank(req.email, "Email")
        if (req.password != req.confirmPassword) throw BadRequestException("Password confirmation does not match")
        if (req.password.length < 8) throw BadRequestException("Password must be at least 8 characters")
        if (adminRepo.findByEmail(req.email.trim().lowercase()) != null) throw BadRequestException("Email already exists")

        val createdId = adminRepo.create(
            fullName = req.fullName.trim(),
            email = req.email.trim().lowercase(),
            passwordHash = Passwords.hash(req.password),
            role = req.role,
            status = req.status,
            createdBy = actor
        )
        val perms = if (req.role == AdminRole.SUPER_ADMIN) ModuleKey.entries.map { locapin.admin.repositories.PermissionRecord(it, true, true, true, true) }
        else normalizePermissions(req.permissions)
        adminRepo.replacePermissions(createdId, perms)
        return createdId
    }

    fun updateAdminUser(id: Long, req: AdminUserUpdateRequest) {
        Validators.requireNotBlank(req.fullName, "Full name")
        Validators.requireNotBlank(req.email, "Email")
        val existing = adminRepo.findById(id) ?: throw BadRequestException("Admin user not found")
        if (existing.role == AdminRole.SUPER_ADMIN && req.status == AdminAccountStatus.INACTIVE && adminRepo.countActiveSuperAdmins() <= 1) {
            throw BadRequestException("Cannot deactivate the last active SUPER_ADMIN")
        }
        adminRepo.updateUser(id, req.fullName.trim(), req.email.trim().lowercase(), req.role, req.status)
        val perms = if (req.role == AdminRole.SUPER_ADMIN) ModuleKey.entries.map { locapin.admin.repositories.PermissionRecord(it, true, true, true, true) }
        else normalizePermissions(req.permissions)
        adminRepo.replacePermissions(id, perms)
    }

    fun updateAdminStatus(id: Long, status: AdminAccountStatus) {
        val existing = adminRepo.findById(id) ?: throw BadRequestException("Admin user not found")
        if (existing.role == AdminRole.SUPER_ADMIN && status == AdminAccountStatus.INACTIVE && adminRepo.countActiveSuperAdmins() <= 1) {
            throw BadRequestException("Cannot deactivate the last active SUPER_ADMIN")
        }
        adminRepo.updateUser(id, existing.fullName, existing.email, existing.role, status)
    }

    fun deleteAdminUser(id: Long) {
        val existing = adminRepo.findById(id) ?: throw BadRequestException("Admin user not found")
        if (existing.role == AdminRole.SUPER_ADMIN && adminRepo.countActiveSuperAdmins() <= 1) {
            throw BadRequestException("Cannot remove the last active SUPER_ADMIN")
        }
        adminRepo.deleteUser(id)
    }

    fun resetAdminPassword(id: Long, req: ResetPasswordRequest) {
        if (req.newPassword != req.confirmNewPassword) throw BadRequestException("Password confirmation does not match")
        if (req.newPassword.length < 8) throw BadRequestException("Password must be at least 8 characters")
        adminRepo.updatePassword(id, Passwords.hash(req.newPassword))
    }

    fun getAdminPermissions(id: Long) = adminRepo.getPermissions(id).map {
        mapOf("moduleKey" to it.moduleKey.name, "canCreate" to it.canCreate, "canRead" to it.canRead, "canUpdate" to it.canUpdate, "canDelete" to it.canDelete)
    }

    fun setAdminPermissions(id: Long, permissions: List<PermissionItemRequest>) {
        adminRepo.replacePermissions(id, normalizePermissions(permissions))
    }

    private fun normalizePermissions(input: List<PermissionItemRequest>): List<locapin.admin.repositories.PermissionRecord> {
        val requested = input.associateBy { it.moduleKey }
        return ModuleKey.entries.map { key ->
            val i = requested[key]
            val canRead = i?.canRead ?: false
            locapin.admin.repositories.PermissionRecord(
                moduleKey = key,
                canRead = canRead,
                canCreate = canRead && (i?.canCreate ?: false),
                canUpdate = canRead && (i?.canUpdate ?: false),
                canDelete = canRead && (i?.canDelete ?: false)
            )
        }
    }
}
