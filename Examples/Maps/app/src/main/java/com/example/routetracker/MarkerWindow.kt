package com.example.routetracker

import android.annotation.SuppressLint
import android.content.Context
import android.text.Layout
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import com.example.routetracker.helpers.locationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.coroutines.coroutineContext

class MarkerWindow(val context: Context, mapView: MapView, val mapFragment: MapFragment) :
    InfoWindow(R.layout.info_window, mapView) {
    lateinit var onRoute: () -> Unit

    override fun onOpen(item: Any?) {
        closeAllInfoWindowsOn(mapView)


        //clicking route button
        view.findViewById<Button>(R.id.btRoute).setOnClickListener {
            onRoute()
            close()
        }

        mView.setOnClickListener { close() }
    }

    //setting the title of the place to the textview
    fun setTitle(title: String) {
        view.findViewById<TextView>(R.id.tvTitle).text = title
    }

    fun setType(type: String) {
        view.findViewById<TextView>(R.id.tvType).text = type
    }

    override fun onClose() {
        close()
    }

}