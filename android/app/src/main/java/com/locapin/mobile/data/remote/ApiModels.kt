package com.locapin.mobile.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiEnvelope<T>(
    val data: T? = null,
    val message: String? = null,
    val error: String? = null
)

@Serializable
data class AuthRequest(val email: String, val password: String)

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

@Serializable
data class ForgotPasswordRequest(val email: String)

@Serializable
data class AuthResponse(val token: String, val user: UserDto)

@Serializable
data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    @SerialName("avatar_url") val avatarUrl: String? = null
)

@Serializable
data class CategoryDto(val id: String, val name: String)

@Serializable
data class DestinationDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val category: CategoryDto? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("hero_image_url") val heroImageUrl: String? = null,
    @SerialName("gallery_images") val galleryImages: List<String> = emptyList(),
    val rating: Double? = null,
    val distanceKm: Double? = null,
    val openingHours: String? = null,
    val contactInfo: String? = null,
    val isFavorite: Boolean = false
)

@Serializable
data class DestinationQuery(
    val query: String? = null,
    val categoryId: String? = null,
    val sort: String? = null,
    val page: Int = 1,
    val pageSize: Int = 20,
    val lat: Double? = null,
    val lng: Double? = null
)


@Serializable
data class MapPointDto(
    val lat: Double,
    val lng: Double
)

@Serializable
data class MapZoneDto(
    val id: String,
    val displayName: String,
    val polygonPoints: List<MapPointDto>,
    val centerLat: Double,
    val centerLng: Double
)

@Serializable
data class MapAttractionDto(
    val id: String,
    val name: String,
    val knownFor: String,
    val latitude: Double,
    val longitude: Double,
    val zoneId: String,
    val imageUrl: String? = null,
    val category: String? = null
)
