package com.example.routetracker.helpers

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


fun hasLocationPermission(context: Context, permission: String): Boolean =
    (ContextCompat.checkSelfPermission(
        context, permission
    ) == PackageManager.PERMISSION_GRANTED)

fun locationProvider(context: Context): String? = when {
    hasLocationPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) -> {
        LocationManager.GPS_PROVIDER
    }
    hasLocationPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) -> {
        LocationManager.NETWORK_PROVIDER
    }
    else -> {
        null
    }
}


fun requestLocationPermissions(context: Activity): Boolean {
    val requiredPermissions: MutableList<String> = mutableListOf()
    var ret = true
    if ((ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED)
    ) {
        requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        ret = false
    }
    if ((ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED)
    ) {
        requiredPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        ret = false
    }
    if ((ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED)
    ) {
        requiredPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        ret = false
    }
    if ((ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED)
    ) {
        requiredPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    if (requiredPermissions.isNotEmpty())
        ActivityCompat.requestPermissions(
            context, requiredPermissions.toTypedArray(),
            0
        )

    return ret

}