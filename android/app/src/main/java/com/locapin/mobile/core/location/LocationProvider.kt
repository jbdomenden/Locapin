package com.locapin.mobile.core.location

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Singleton
class LocationProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    @SuppressLint("MissingPermission")
    suspend fun getLastKnownLocation(): Pair<Double, Double>? = suspendCancellableCoroutine { cont ->
        LocationServices.getFusedLocationProviderClient(context)
            .lastLocation
            .addOnSuccessListener { location ->
                if (location == null) cont.resume(null)
                else cont.resume(location.latitude to location.longitude)
            }
            .addOnFailureListener { cont.resume(null) }
    }
}
