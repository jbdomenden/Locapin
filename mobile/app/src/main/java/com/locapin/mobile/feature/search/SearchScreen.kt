package com.locapin.mobile.feature.search

import androidx.compose.foundation.layout.Arrangement
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
fun SearchScreen(vm: MainViewModel, onDetails: (String) -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val q by vm.queryFlow.collectAsStateWithLifecycle()
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            OutlinedTextField(
                value = q,
                onValueChange = vm::setSearchQuery,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search destinations or landmarks") }
            )
        }
        if (state.searchResults.isEmpty() && q.isNotBlank()) {
            item { Text("No results found") }
        }
        items(state.searchResults) { result ->
            DestinationCard(destination = result, onClick = { onDetails(result.id) })
        }
    }
}
