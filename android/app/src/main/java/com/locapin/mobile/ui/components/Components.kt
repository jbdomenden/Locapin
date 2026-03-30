package com.locapin.mobile.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.locapin.mobile.domain.model.Destination

@Composable
fun LoadingBlock(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        CircularProgressIndicator(modifier = Modifier.padding(24.dp))
    }
}

@Composable
fun DestinationCard(destination: Destination, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column {
            AsyncImage(
                model = destination.heroImageUrl,
                contentDescription = destination.name,
                modifier = Modifier.fillMaxWidth()
            )
            Column(Modifier.padding(12.dp)) {
                Text(destination.name, style = MaterialTheme.typography.titleMedium)
                Text(destination.description, maxLines = 2, style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = {}, label = { Text(destination.categoryName.ifBlank { "Attraction" }) })
                    Row {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                        Text(destination.distanceKm?.let { "%.1f km".format(it) } ?: "San Juan")
                    }
                }
            }
        }
    }
}
