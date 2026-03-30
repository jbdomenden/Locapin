package com.locapin.mobile.feature.destination

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.locapin.mobile.ui.MainViewModel

@Composable
fun DestinationDetailsScreen(vm: MainViewModel, destinationId: String, onBack: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    androidx.compose.runtime.LaunchedEffect(destinationId) { vm.openDestination(destinationId) }
    val destination = state.selectedDestination ?: return

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            AsyncImage(
                model = destination.heroImageUrl,
                contentDescription = destination.name,
                modifier = Modifier.fillMaxWidth()
            )
            Text(destination.name, style = MaterialTheme.typography.headlineMedium)
            Text(destination.description)
            Text("Address: ${destination.address}")
            destination.openingHours?.let { Text("Hours: $it") }
            destination.contactInfo?.let { Text("Contact: $it") }
        }
        items(destination.galleryImages) { img ->
            AsyncImage(model = img, contentDescription = "Gallery image", modifier = Modifier.fillMaxWidth())
        }
        item {
            Button(onClick = { vm.toggleFavorite(destination) }, modifier = Modifier.fillMaxWidth()) {
                Text(if (destination.isFavorite) "Remove from favorites" else "Save to favorites")
            }
            Button(onClick = {
                val uri = Uri.parse("geo:${destination.lat},${destination.lng}?q=${Uri.encode(destination.name)}")
                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            }, modifier = Modifier.fillMaxWidth()) { Text("Open in Maps") }
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
        }
    }
}
