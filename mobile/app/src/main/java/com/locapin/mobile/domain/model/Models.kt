package com.locapin.mobile.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String?
)

data class Category(
    val id: String,
    val name: String
)

data class Destination(
    val id: String,
    val name: String,
    val description: String,
    val categoryName: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val heroImageUrl: String?,
    val galleryImages: List<String>,
    val rating: Double?,
    val distanceKm: Double?,
    val openingHours: String?,
    val contactInfo: String?,
    val isFavorite: Boolean
)
