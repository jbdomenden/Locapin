package com.locapin.mobile.data.repository

import com.locapin.mobile.core.common.LocaPinResult
import com.locapin.mobile.data.local.SanJuanSeedDataSource
import com.locapin.mobile.data.remote.LocaPinApi
import com.locapin.mobile.domain.model.MapArea
import com.locapin.mobile.domain.model.MapAttraction
import com.locapin.mobile.domain.model.MapPoint
import com.locapin.mobile.domain.repository.SegmentedMapRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SegmentedMapRepositoryImpl @Inject constructor(
    private val api: LocaPinApi,
    private val seedDataSource: SanJuanSeedDataSource
) : SegmentedMapRepository {

    override suspend fun getMapAreas(): LocaPinResult<List<MapArea>> = runCatching {
        api.mapAreas().data?.map {
            MapArea(
                id = it.id,
                displayName = it.displayName,
                polygon = it.polygon.map { p -> MapPoint(p.x, p.y) },
                center = MapPoint(it.center.x, it.center.y)
            )
        }
    }.fold(
        onSuccess = { LocaPinResult.Success(it ?: seedDataSource.mapAreas()) },
        onFailure = { LocaPinResult.Success(seedDataSource.mapAreas()) }
    )

    override suspend fun getMapAttractions(): LocaPinResult<List<MapAttraction>> = runCatching {
        api.mapAttractions().data?.map {
            MapAttraction(
                id = it.id,
                name = it.name,
                knownFor = it.knownFor,
                latitude = it.latitude,
                longitude = it.longitude,
                areaId = it.areaId,
                imageUrl = it.imageUrl,
                category = it.category,
                mapPoint = MapPoint(it.mapPoint.x, it.mapPoint.y)
            )
        }
    }.fold(
        onSuccess = { LocaPinResult.Success(it ?: seedDataSource.attractions()) },
        onFailure = { LocaPinResult.Success(seedDataSource.attractions()) }
    )
}
