package com.locapin.mobile.feature.map

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.locapin.mobile.domain.model.ZoneAttraction

fun launchDirections(context: Context, attraction: ZoneAttraction, userLocation: Pair<Double, Double>?) {
    val uri = if (userLocation != null) {
        Uri.parse("google.navigation:q=${attraction.latitude},${attraction.longitude}&origin=${userLocation.first},${userLocation.second}")
    } else {
        Uri.parse("google.navigation:q=${attraction.latitude},${attraction.longitude}")
    }

    val mapsIntent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
    }

    val fallback = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("geo:${attraction.latitude},${attraction.longitude}?q=${attraction.latitude},${attraction.longitude}(${Uri.encode(attraction.name)})")
    )

    val resolver = context.packageManager
    when {
        mapsIntent.resolveActivity(resolver) != null -> context.startActivity(mapsIntent)
        fallback.resolveActivity(resolver) != null -> context.startActivity(fallback)
    }
}
