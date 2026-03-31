package com.locapin.mobile.data.local

import com.locapin.mobile.domain.model.MapZone
import com.locapin.mobile.domain.model.ZoneAttraction
import com.locapin.mobile.domain.model.ZonePoint
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SanJuanSeedDataSource @Inject constructor() {

    fun mapZones(): List<MapZone> = listOf(
        MapZone(
            id = "pinaglabanan",
            displayName = "Pinaglabanan",
            polygonPoints = listOf(
                ZonePoint(14.6062, 121.0296),
                ZonePoint(14.6044, 121.0339),
                ZonePoint(14.6023, 121.0350),
                ZonePoint(14.6008, 121.0318),
                ZonePoint(14.6024, 121.0281)
            ),
            centerLat = 14.6030,
            centerLng = 121.0316
        ),
        MapZone(
            id = "city_center",
            displayName = "City Center",
            polygonPoints = listOf(
                ZonePoint(14.6038, 121.0348),
                ZonePoint(14.6026, 121.0402),
                ZonePoint(14.5995, 121.0401),
                ZonePoint(14.5988, 121.0352),
                ZonePoint(14.6009, 121.0332)
            ),
            centerLat = 14.6012,
            centerLng = 121.0370
        ),
        MapZone(
            id = "greenhills",
            displayName = "Greenhills",
            polygonPoints = listOf(
                ZonePoint(14.6055, 121.0430),
                ZonePoint(14.6040, 121.0505),
                ZonePoint(14.5996, 121.0525),
                ZonePoint(14.5962, 121.0462),
                ZonePoint(14.5994, 121.0414)
            ),
            centerLat = 14.6010,
            centerLng = 121.0472
        )
    )

    fun attractions(): List<ZoneAttraction> = listOf(
        ZoneAttraction(
            id = "pinaglabanan-shrine",
            name = "Pinaglabanan Shrine",
            knownFor = "Historic landmark commemorating the Battle of San Juan del Monte.",
            latitude = 14.6029,
            longitude = 121.0330,
            zoneId = "pinaglabanan"
        ),
        ZoneAttraction(
            id = "museo-katipunan",
            name = "Museo ng Katipunan",
            knownFor = "Museum focused on Katipunan history and revolutionary artifacts.",
            latitude = 14.6039,
            longitude = 121.0318,
            zoneId = "pinaglabanan"
        ),
        ZoneAttraction(
            id = "san-juan-city-hall",
            name = "San Juan City Hall",
            knownFor = "Civic center and key administrative landmark in San Juan City.",
            latitude = 14.6012,
            longitude = 121.0362,
            zoneId = "city_center"
        ),
        ZoneAttraction(
            id = "santuario-del-santo-cristo",
            name = "Santuario del Santo Cristo Parish",
            knownFor = "Historic parish church known for its religious heritage.",
            latitude = 14.6008,
            longitude = 121.0350,
            zoneId = "city_center"
        ),
        ZoneAttraction(
            id = "greenhills-shopping-center",
            name = "Greenhills Shopping Center",
            knownFor = "Popular shopping and dining hub with bargain markets.",
            latitude = 14.6019,
            longitude = 121.0482,
            zoneId = "greenhills"
        ),
        ZoneAttraction(
            id = "club-filipino",
            name = "Club Filipino",
            knownFor = "Historic social club and events venue.",
            latitude = 14.5978,
            longitude = 121.0470,
            zoneId = "greenhills"
        )
    )
}
