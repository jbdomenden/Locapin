package com.locapin.mobile.domain.model

data class MapPoint(
    val x: Float,
    val y: Float
)

data class MapArea(
    val id: String,
    val displayName: String,
    val polygon: List<MapPoint>,
    val center: MapPoint
)

data class MapAttraction(
    val id: String,
    val name: String,
    val knownFor: String,
    val latitude: Double,
    val longitude: Double,
    val areaId: String,
    val imageUrl: String? = null,
    val category: String? = null,
    val mapPoint: MapPoint
)
