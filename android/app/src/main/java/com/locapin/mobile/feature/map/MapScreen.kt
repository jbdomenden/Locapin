package com.locapin.mobile.feature.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    hasLocationPermission: Boolean,
    requestPermission: () -> Unit ,
    onDetails: (String) -> Unit,
    vm: SegmentedMapViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by vm.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }

    LaunchedEffect(hasLocationPermission) {
        vm.onPermissionResult(hasLocationPermission)
    }

    Column(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MapInstruction()
        SegmentedSanJuanMap(
            zones = state.zones,
            selectedZoneId = state.selectedZoneId,
            visibleAttractions = state.visibleAttractions,
            selectedAttractionId = state.selectedAttractionId,
            onZoneTapped = {
                vm.onZoneSelected(it)
                showSheet = true
            },
            onPinTapped = {
                vm.onAttractionSelected(it)
                showSheet = true
            }
        )

        if (!hasLocationPermission) {
            Button(onClick = requestPermission, modifier = Modifier.fillMaxWidth()) {
                Text("Enable location for real-time distance")
            }
        }
    }

    val selectedAttraction = state.selectedAttraction
    if (showSheet && selectedAttraction != null) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(selectedAttraction.name, style = MaterialTheme.typography.titleLarge)
                Text("Known For", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text(selectedAttraction.knownFor)
                Text(vm.distanceTextFor(selectedAttraction), style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = { launchDirections(context, selectedAttraction, state.userLocation) }) {
                        Text("Directions")
                    }
                    Button(onClick = vm::refreshLocation) {
                        Text("Refresh distance")
                    }
                }
            }
        }
    }
}
