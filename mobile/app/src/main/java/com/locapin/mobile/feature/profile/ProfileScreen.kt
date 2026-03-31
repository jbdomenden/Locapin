package com.locapin.mobile.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.locapin.mobile.ui.MainViewModel

@Composable
fun ProfileScreen(vm: MainViewModel, onSettings: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Account", style = MaterialTheme.typography.headlineSmall)
        Text(state.profile?.name ?: "Guest")
        Text(state.profile?.email ?: "")
        Button(onClick = onSettings, modifier = Modifier.fillMaxWidth()) { Text("Settings") }
        Button(onClick = vm::logout, modifier = Modifier.fillMaxWidth()) { Text("Logout") }
        Text("LocaPin v1.0.0")
    }
}
