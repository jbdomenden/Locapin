package com.locapin.mobile.feature.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    hasLocationPermission: Boolean,
    requestPermission: () -> Unit,
    onDetails: (String) -> Unit,
    vm: SegmentedMapViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }

    LaunchedEffect(hasLocationPermission) {
        vm.onPermissionResult(hasLocationPermission)
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MapInstruction()
        SegmentedSanJuanMap(
            areas = state.areas,
            selectedAreaId = state.selectedAreaId,
            visibleAttractions = state.visibleAttractions,
            selectedAttractionId = state.selectedAttractionId,
            onAreaTapped = {
                vm.onAreaSelected(it)
                showSheet = true
            },
            onPinTapped = vm::onAttractionSelected
        )

        if (!hasLocationPermission) {
            Button(onClick = requestPermission, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Text("Enable location for distance")
            }
        }

        state.visibleAttractions.takeIf { it.isNotEmpty() }?.let { areaAttractions ->
            LazyRow(modifier = Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(areaAttractions) { attraction ->
                    FilterChip(
                        selected = attraction.id == state.selectedAttractionId,
                        onClick = {
                            vm.onAttractionSelected(attraction.id)
                            showSheet = true
                        },
                        label = { Text(attraction.name) }
                    )
                }
            }
        }
    }

    if (showSheet && state.selectedAttraction != null) {
        val attraction = state.selectedAttraction!!
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(attraction.name, style = MaterialTheme.typography.titleLarge)
                Text("Known For", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text(attraction.knownFor)
                Text(vm.distanceTextFor(attraction), style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = { onDetails(attraction.id) }) { Text("View full details") }
                    Button(onClick = { vm.refreshLocation() }) { Text("Refresh distance") }
                }
            }
        }
    }
}
