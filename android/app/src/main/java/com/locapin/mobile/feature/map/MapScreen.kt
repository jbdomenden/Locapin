package com.locapin.mobile.feature.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.locapin.mobile.ui.MainViewModel

@Composable
fun MapScreen(
    vm: MainViewModel,
    hasLocationPermission: Boolean,
    requestPermission: () -> Unit,
    onDetails: (String) -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val camera = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(14.6019, 121.0355), 12f)
    }
    var selectedId by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
        GoogleMap(modifier = Modifier.weight(1f).fillMaxWidth(), cameraPositionState = camera) {
            state.destinations.forEach { place ->
                Marker(
                    state = MarkerState(position = LatLng(place.lat, place.lng)),
                    title = place.name,
                    snippet = place.categoryName,
                    onClick = {
                        selectedId = place.id
                        false
                    }
                )
            }
        }

        if (!hasLocationPermission) {
            Button(onClick = requestPermission, modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                Text("Enable location for nearby attractions")
            }
        }

        state.destinations.find { it.id == selectedId }?.let { selected ->
            Card(Modifier.fillMaxWidth().padding(12.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text(selected.name, style = MaterialTheme.typography.titleMedium)
                    Text(selected.address, style = MaterialTheme.typography.bodySmall)
                    Button(onClick = { onDetails(selected.id) }, modifier = Modifier.fillMaxWidth()) {
                        Text("View details")
                    }
                }
            }
        }
    }
}
