package com.locapin.mobile.feature.map

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
import com.locapin.mobile.domain.model.MapZone
import com.locapin.mobile.domain.model.ZoneAttraction

private val SanJuanCenter = LatLng(14.6019, 121.0355)

@Composable
fun SegmentedSanJuanMap(
    zones: List<MapZone>,
    selectedZoneId: String?,
    visibleAttractions: List<ZoneAttraction>,
    selectedAttractionId: String?,
    onZoneTapped: (String) -> Unit,
    onPinTapped: (String) -> Unit
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(SanJuanCenter, 13.6f)
    }

    GoogleMap(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(zoomControlsEnabled = false, mapToolbarEnabled = false),
        properties = MapProperties(isMyLocationEnabled = false),
        onMapClick = { click ->
            zones.firstOrNull { it.contains(click) }?.let { onZoneTapped(it.id) }
        }
    ) {
        zones.forEach { zone ->
            val selected = zone.id == selectedZoneId
            Polygon(
                points = zone.polygonPoints.map { LatLng(it.lat, it.lng) },
                fillColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.30f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.20f),
                strokeColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                strokeWidth = if (selected) 8f else 4f,
                clickable = true,
                onClick = { onZoneTapped(zone.id) }
            )
        }

        visibleAttractions.forEach { attraction ->
            val latLng = LatLng(attraction.latitude, attraction.longitude)
            val selected = attraction.id == selectedAttractionId
            Marker(
                state = MarkerState(position = latLng),
                title = attraction.name,
                snippet = attraction.knownFor,
                alpha = if (selected) 1f else 0.85f,
                onClick = {
                    onPinTapped(attraction.id)
                    true
                }
            )
        }
    }
}

private fun MapZone.contains(point: LatLng): Boolean {
    var intersects = 0
    val poly = polygonPoints
    poly.indices.forEach { i ->
        val a = poly[i]
        val b = poly[(i + 1) % poly.size]
        val cond = (a.lat > point.latitude) != (b.lat > point.latitude)
        if (cond) {
            val xinters = (point.latitude - a.lat) * (b.lng - a.lng) / ((b.lat - a.lat).takeIf { it != 0.0 } ?: 0.0000001) + a.lng
            if (point.longitude < xinters) intersects++
        }
    }
    return intersects % 2 == 1
}

@Composable
fun MapInstruction() {
    Text(
        text = "Tap a zone to explore attractions",
        style = MaterialTheme.typography.bodyMedium
    )
}
