package com.locapin.mobile.data.repository

import com.locapin.mobile.data.remote.DestinationDto
import com.locapin.mobile.data.remote.UserDto
import com.locapin.mobile.domain.model.Destination
import com.locapin.mobile.domain.model.User

fun UserDto.toDomain() = User(
    id = id,
    name = name,
    email = email,
    avatarUrl = avatarUrl
)

fun DestinationDto.toDomain() = Destination(
    id = id,
    name = name,
    description = description.orEmpty(),
    categoryName = category?.name.orEmpty(),
    address = address.orEmpty(),
    lat = latitude ?: 0.0,
    lng = longitude ?: 0.0,
    heroImageUrl = heroImageUrl,
    galleryImages = galleryImages,
    rating = rating,
    distanceKm = distanceKm,
    openingHours = openingHours,
    contactInfo = contactInfo,
    isFavorite = isFavorite
)
