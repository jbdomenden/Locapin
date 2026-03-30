package locapin.admin.models

import kotlinx.serialization.Serializable

@Serializable data class ApiResponse<T>(val success: Boolean, val message: String, val data: T? = null)
@Serializable data class LoginRequest(val email: String, val password: String)
@Serializable data class ChangePasswordRequest(val currentPassword: String, val newPassword: String, val confirmNewPassword: String)
@Serializable data class PermissionItemRequest(val moduleKey: ModuleKey, val canCreate: Boolean, val canRead: Boolean, val canUpdate: Boolean, val canDelete: Boolean)
@Serializable data class AdminUserCreateRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
    val role: AdminRole,
    val status: AdminAccountStatus = AdminAccountStatus.ACTIVE,
    val permissions: List<PermissionItemRequest> = emptyList()
)
@Serializable data class AdminUserUpdateRequest(
    val fullName: String,
    val email: String,
    val role: AdminRole,
    val status: AdminAccountStatus,
    val permissions: List<PermissionItemRequest> = emptyList()
)
@Serializable data class ResetPasswordRequest(val newPassword: String, val confirmNewPassword: String)
@Serializable data class CityRequest(val name: String, val isPremium: Boolean)
@Serializable data class StatusRequest(val status: EntityStatus)
@Serializable data class PremiumRequest(val isPremium: Boolean)
@Serializable data class AreaRequest(val cityId: Long, val name: String, val centerLatitude: Double, val centerLongitude: Double, val boundaryData: String? = null)
@Serializable data class AttractionRequest(val cityId: Long, val areaId: Long, val name: String, val description: String, val highlights: String, val latitude: Double, val longitude: Double, val openHours: String? = null)
@Serializable data class FeaturedRequest(val isFeatured: Boolean)
@Serializable data class PlanRequest(val name: String, val description: String, val price: Double, val billingPeriod: BillingPeriod, val isActive: Boolean)
@Serializable data class PhotoReorderItem(val id: Long, val sortOrder: Int)
@Serializable data class PhotoReorderRequest(val items: List<PhotoReorderItem>)
@Serializable data class AdminSessionUser(val id: Long, val name: String, val email: String, val role: AdminRole)
