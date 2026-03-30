package com.locapin.mobile.domain.repository

import com.locapin.mobile.core.common.LocaPinResult
import com.locapin.mobile.domain.model.MapArea
import com.locapin.mobile.domain.model.MapAttraction

interface SegmentedMapRepository {
    suspend fun getMapAreas(): LocaPinResult<List<MapArea>>
    suspend fun getMapAttractions(): LocaPinResult<List<MapAttraction>>
}
