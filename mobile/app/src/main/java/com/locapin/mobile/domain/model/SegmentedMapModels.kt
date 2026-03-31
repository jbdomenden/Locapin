package com.locapin.mobile.domain.model

data class ZonePoint(
    val lat: Double,
    val lng: Double
)

data class MapZone(
    val id: String,
    val displayName: String,
    val polygonPoints: List<ZonePoint>,
    val centerLat: Double,
    val centerLng: Double
)

data class ZoneAttraction(
    val id: String,
    val name: String,
    val knownFor: String,
    val latitude: Double,
    val longitude: Double,
    val zoneId: String,
    val imageUrl: String? = null,
    val category: String? = null
)
