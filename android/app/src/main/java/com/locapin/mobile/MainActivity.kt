package com.locapin.mobile

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.locapin.mobile.core.designsystem.theme.LocaPinTheme
import com.locapin.mobile.ui.LocaPinRoot
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var hasLocationPermission by mutableStateOf(false)
    private val locationPermissionRequester =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            hasLocationPermission = granted
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LocaPinTheme {
                LocaPinRoot(
                    hasLocationPermission = hasLocationPermission,
                    requestLocationPermission = {
                        locationPermissionRequester.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                )
            }
        }
    }
}
