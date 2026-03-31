package com.locapin.mobile.data.repository

import com.locapin.mobile.core.common.LocaPinResult
import com.locapin.mobile.data.local.SanJuanSeedDataSource
import com.locapin.mobile.data.remote.LocaPinApi
import com.locapin.mobile.domain.model.MapZone
import com.locapin.mobile.domain.model.ZoneAttraction
import com.locapin.mobile.domain.model.ZonePoint
import com.locapin.mobile.domain.repository.SegmentedMapRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SegmentedMapRepositoryImpl @Inject constructor(
    private val api: LocaPinApi,
    private val seedDataSource: SanJuanSeedDataSource
) : SegmentedMapRepository {

    override suspend fun getMapZones(): LocaPinResult<List<MapZone>> = runCatching {
        api.mapAreas().data?.map {
            MapZone(
                id = it.id,
                displayName = it.displayName,
                polygonPoints = it.polygonPoints.map { p -> ZonePoint(p.lat, p.lng) },
                centerLat = it.centerLat,
                centerLng = it.centerLng
            )
        }
    }.fold(
        onSuccess = { LocaPinResult.Success(it ?: seedDataSource.mapZones()) },
        onFailure = { LocaPinResult.Success(seedDataSource.mapZones()) }
    )

    override suspend fun getZoneAttractions(): LocaPinResult<List<ZoneAttraction>> = runCatching {
        api.mapAttractions().data?.map {
            ZoneAttraction(
                id = it.id,
                name = it.name,
                knownFor = it.knownFor,
                latitude = it.latitude,
                longitude = it.longitude,
                zoneId = it.zoneId,
                imageUrl = it.imageUrl,
                category = it.category
            )
        }
    }.fold(
        onSuccess = { LocaPinResult.Success(it ?: seedDataSource.attractions()) },
        onFailure = { LocaPinResult.Success(seedDataSource.attractions()) }
    )
}
