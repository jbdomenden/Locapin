package com.locapin.mobile.data.local

import com.locapin.mobile.domain.model.MapArea
import com.locapin.mobile.domain.model.MapAttraction
import com.locapin.mobile.domain.model.MapPoint
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SanJuanSeedDataSource @Inject constructor() {
    fun mapAreas(): List<MapArea> = listOf(
        MapArea(
            id = "pinaglabanan",
            displayName = "Pinaglabanan",
            polygon = listOf(
                MapPoint(0.08f, 0.58f), MapPoint(0.24f, 0.42f), MapPoint(0.35f, 0.50f),
                MapPoint(0.26f, 0.68f), MapPoint(0.12f, 0.70f)
            ),
            center = MapPoint(0.22f, 0.57f)
        ),
        MapArea(
            id = "city_center",
            displayName = "City Center",
            polygon = listOf(
                MapPoint(0.35f, 0.50f), MapPoint(0.50f, 0.42f), MapPoint(0.64f, 0.50f),
                MapPoint(0.53f, 0.68f), MapPoint(0.38f, 0.66f)
            ),
            center = MapPoint(0.49f, 0.56f)
        ),
        MapArea(
            id = "greenhills",
            displayName = "Greenhills",
            polygon = listOf(
                MapPoint(0.64f, 0.50f), MapPoint(0.84f, 0.40f), MapPoint(0.92f, 0.56f),
                MapPoint(0.82f, 0.76f), MapPoint(0.62f, 0.70f)
            ),
            center = MapPoint(0.79f, 0.58f)
        )
    )

    fun attractions(): List<MapAttraction> = listOf(
        MapAttraction(
            id = "pinaglabanan-shrine",
            name = "Pinaglabanan Shrine",
            knownFor = "Historic landmark commemorating the Battle of San Juan del Monte.",
            latitude = 14.6029,
            longitude = 121.0330,
            areaId = "pinaglabanan",
            mapPoint = MapPoint(0.21f, 0.56f)
        ),
        MapAttraction(
            id = "museo-katipunan",
            name = "Museo ng Katipunan",
            knownFor = "Museum focused on Katipunan history and revolutionary artifacts.",
            latitude = 14.6039,
            longitude = 121.0318,
            areaId = "pinaglabanan",
            mapPoint = MapPoint(0.25f, 0.60f)
        ),
        MapAttraction(
            id = "san-juan-city-hall",
            name = "San Juan City Hall",
            knownFor = "Civic center and key administrative landmark in San Juan City.",
            latitude = 14.6012,
            longitude = 121.0362,
            areaId = "city_center",
            mapPoint = MapPoint(0.48f, 0.58f)
        ),
        MapAttraction(
            id = "santuario-del-santo-cristo",
            name = "Santuario del Santo Cristo Parish",
            knownFor = "Historic parish church known for its religious heritage.",
            latitude = 14.6008,
            longitude = 121.0350,
            areaId = "city_center",
            mapPoint = MapPoint(0.53f, 0.54f)
        ),
        MapAttraction(
            id = "greenhills-shopping-center",
            name = "Greenhills Shopping Center",
            knownFor = "Popular shopping and dining hub with bargain markets.",
            latitude = 14.6019,
            longitude = 121.0482,
            areaId = "greenhills",
            mapPoint = MapPoint(0.78f, 0.57f)
        ),
        MapAttraction(
            id = "club-filipino",
            name = "Club Filipino",
            knownFor = "Historic social club and events venue.",
            latitude = 14.5978,
            longitude = 121.0470,
            areaId = "greenhills",
            mapPoint = MapPoint(0.74f, 0.64f)
        )
    )
}
