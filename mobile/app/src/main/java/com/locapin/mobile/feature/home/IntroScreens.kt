package com.locapin.mobile.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.locapin.mobile.R

@Composable
fun SplashScreen() {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = painterResource(id = R.drawable.ic_locapin_logo), contentDescription = "LocaPin logo")
        Text("LocaPin", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun OnboardingScreen(onDone: () -> Unit, onSkip: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Discover places in San Juan", style = MaterialTheme.typography.headlineSmall)
        Text("Explore nearby attractions")
        Text("View locations on an interactive map")
        Text("Save your favorite destinations")
        Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Get started") }
        androidx.compose.material3.TextButton(onClick = onSkip) { Text("Skip") }
    }
}
