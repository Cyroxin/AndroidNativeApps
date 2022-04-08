package com.example.routetracker.helpers

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import com.example.routetracker.BuildConfig
import com.example.routetracker.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.bonuspack.location.NominatimPOIProvider
import org.osmdroid.bonuspack.location.POI
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.IOException
import java.lang.Error

fun getAddress(context: Context?, point: GeoPoint): String {
    return try {
        val geocoder = Geocoder(context)
        val list = geocoder.getFromLocation(point.latitude, point.longitude, 1)
        list[0].getAddressLine(0)
    } catch (e: IOException) {
        "" // Return empty if at a location without an address.
    }
}

fun parseLocation(address: String): GeoPoint? {

    val arg: List<String> = if (address.startsWith("geo:", ignoreCase = true))
        address.drop("geo:".count()).split(',', '?') // [Long, lat, parameters]
    else
        address.split(',', '?') // [Long, lat, parameters]

    return try {
        GeoPoint(arg[0].toDouble(), arg[1].toDouble())
    } catch (err: Error) {
        null
    }
}

val userAgent = BuildConfig.APPLICATION_ID + "/" + BuildConfig.VERSION_NAME

fun fetchPointsOfInterest(request: String, box: BoundingBox, result: (List<POI>) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        val poiProvider = NominatimPOIProvider(userAgent)
        try {
            val poitemp = mutableListOf<POI>()
            request.split('|').forEach {
                poitemp.addAll(poiProvider.getPOIInside(box, it, 10))
            }
            Log.d("Points of Interest", poitemp.size.toString())
            result(poitemp)
        } catch (exp: NullPointerException) { // Network Error
            result(listOf())
            Log.e("Network Error", "Failed to fetch points of interest")
        }
    }
}

fun fetchPointsOfInterestUrl(query: String, result: (List<POI>) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        val poiProvider = NominatimPOIProvider(userAgent)
        try {
            val poitemp = mutableListOf<POI>()
            poiProvider.getThem(
                "https://nominatim.openstreetmap.org/search?format=json&q=${
                    query.replace(
                        " ",
                        "%20"
                    )
                }&limit=10"
            )
            Log.d("Points of Interest", poitemp.size.toString())
            result(poitemp)
        } catch (exp: NullPointerException) { // Network Error
            result(listOf())
            Log.e("Network Error", "Failed to fetch points of interest")
        }
    }
}


fun createPath(
    context: Context?,
    startPoint: GeoPoint,
    endPoint: GeoPoint,
    onFinished: (Polyline?) -> Unit
) {
    val routePoints = ArrayList<GeoPoint>()
    routePoints.add(startPoint)
    routePoints.add(endPoint)
    CoroutineScope(Dispatchers.IO).launch {
        onFinished(getPath(context, routePoints))
    }
}

fun getPath(context: Context?, waypoints: ArrayList<GeoPoint>): Polyline? {
    // Retrieving road
    val roadManager = OSRMRoadManager(context, userAgent)
    roadManager.setMean(OSRMRoadManager.MEAN_BY_FOOT)
    val road = roadManager.getRoad(waypoints)
    return RoadManager.buildRoadOverlay(road, 0xAA0000FF.toInt(), 10.5F)

    /*
    //Marker at each node
    val nodeIcon = AppCompatResources.getDrawable(mapView.context, R.drawable.ic_baseline_stop_24)
    for (i in 0 until road.mNodes.size) {
        val node = road.mNodes[i]
        val nodeMarker = Marker(mapView)
        nodeMarker.position = node.mLocation
        nodeMarker.icon = nodeIcon
        nodeMarker.title = "Step $i"
        mapView.overlays.add(nodeMarker)
        nodeMarker.snippet = node.mInstructions
        nodeMarker.subDescription =
            Road.getLengthDurationText(mapView.context, node.mLength, node.mDuration)
    }
    */
}
