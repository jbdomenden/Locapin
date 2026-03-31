package com.locapin.mobile.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.locapin.mobile.ui.MainViewModel
import com.locapin.mobile.ui.components.DestinationCard

@Composable
fun HomeScreen(vm: MainViewModel, onOpenMap: () -> Unit, onDetails: (String) -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Search destinations") },
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        )
        Text("Featured in San Juan", modifier = Modifier.padding(horizontal = 16.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(state.destinations.take(10)) { destination ->
                DestinationCard(destination = destination, onClick = { onDetails(destination.id) })
            }
            item {
                androidx.compose.material3.Button(onClick = onOpenMap, modifier = Modifier.fillMaxWidth()) {
                    Text("Open full map")
                }
            }
        }
    }
}
