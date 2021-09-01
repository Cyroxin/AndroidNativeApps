package com.example.routetracker

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.io.IOException


class MainActivity : AppCompatActivity(), LocationListener, SensorEventListener {

    private lateinit var lm: LocationManager

    private lateinit var sm: SensorManager
    private var stepSensor: Sensor? = null
    private lateinit var stepCount: TextView
    var stepsTotal : Float? = null // Alltime stepcount

    lateinit var map : MapView
    private lateinit var marker: Marker
    private lateinit var path : Polyline

    private lateinit var toggle: FloatingActionButton


    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        setContentView(R.layout.activity_main)

        // Stepcounter
        sm = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepCount = findViewById<TextView>(R.id.steps)

        // Map
        lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        map = findViewById<MapView>(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.isTilesScaledToDpi = true
        map.setMultiTouchControls(true)
        map.controller.setZoom(3.0)
        createOverlays()

        // Fab
        toggle = findViewById<FloatingActionButton>(R.id.toggle)
        toggle.tag = false // Recording?
        toggle.backgroundTintList = ColorStateList.valueOf(Color.GREEN + Color.GREEN * 40/100);
        toggle.setOnClickListener {
            if(toggle.tag == false && requestPermissions()) // Start recording
            {
                if(locationProvider() != null) {
                    enableStepSensor()
                    toggle.tag = true
                        toggle.backgroundTintList =
                            ColorStateList.valueOf(Color.RED + Color.RED * 40 / 100);
                        lm.requestLocationUpdates(locationProvider()!!, 1000, 15f, this)
                        map.controller.setZoom(18.0)
                }
                else {
                    Log.e("Location","Insufficient permissions, location needed")
                    Toast.makeText(
                        this.applicationContext,
                        "Insufficient permissions, location needed",
                        Toast.LENGTH_LONG
                    ).show()

                    requestPermissions()
                }

            }
            else // Stop recording
            {
                toggle.tag = false

                // Stop recording
                toggle.backgroundTintList = ColorStateList.valueOf(Color.GREEN + Color.GREEN * 40/100);
                lm.removeUpdates(this)

                // Clear map
                path.setPoints(listOf())
                marker.alpha = 0f
                marker.closeInfoWindow()
                map.invalidate()

                // Stop stepcounter
                disableStepSensor()
            }
        }


    }

    override fun onProviderDisabled(provider: String) {
        //super.onProviderDisabled(provider)

        Toast.makeText(
            this.applicationContext,
            "Please enable $provider",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onProviderEnabled(provider: String) {
        //super.onProviderEnabled(provider)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }

    private fun createOverlays(){

        // Path
        path = Polyline(map, true)
        path.outlinePaint.color = Color.RED
        path.infoWindow = null
        map.overlays.add(path)

        // Person
        marker = Marker(map)
        marker.icon = AppCompatResources.getDrawable(this, R.drawable.person)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.alpha = 0f
        map.overlays.add(marker)
    }


    private fun locationProvider() : String? = when {
        hasLocationPermission(Manifest.permission.ACCESS_FINE_LOCATION) -> {
            LocationManager.GPS_PROVIDER
        }
        hasLocationPermission(Manifest.permission.ACCESS_COARSE_LOCATION) -> {
            LocationManager.NETWORK_PROVIDER
        }
        else -> {
            null
        }
    }

    private fun hasLocationPermission(permission: String) : Boolean = (ContextCompat.checkSelfPermission(
        this, permission.toString()) == PackageManager.PERMISSION_GRANTED)


    private fun requestPermissions() : Boolean
    {
        var requiredPermissions: List<String> = listOf()
        var ret = true
        if(Build.VERSION.SDK_INT >= 23) {
            if ((ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED)
            ) {
                requiredPermissions += android.Manifest.permission.ACCESS_FINE_LOCATION
                ret = false
            }
            if ((ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED)
            ) {
                requiredPermissions += android.Manifest.permission.ACCESS_COARSE_LOCATION
                ret = false
            }
            if ((ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED)
            ) {
                requiredPermissions += android.Manifest.permission.READ_EXTERNAL_STORAGE
                ret = false
            }
            if ((ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED)
            ) {
                requiredPermissions += android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            }

            if(requiredPermissions.isNotEmpty())
                ActivityCompat.requestPermissions(
                this, requiredPermissions.toTypedArray(),
                0)
        }

        return ret

    }

    override fun onLocationChanged(p0: Location) {
        Log.d("GEOLOCATION", "new latitude: ${p0.latitude} and longitude: ${p0.longitude}")
        val point = GeoPoint(p0.latitude, p0.longitude)
        map.controller.setCenter(point)

        // Marker
        marker.position = point
        marker.title = point.longitude.roundToDecimal(5).toString() + ',' + point.latitude.roundToDecimal(5).toString()
        Log.e("Point",point.toString())
        CoroutineScope(Dispatchers.IO).async {
            marker.subDescription = getAddress(point)
        }
        marker.alpha = 1f

        // Path
        path.addPoint(point)

        map.invalidate()
    }

    private fun getAddress(point : GeoPoint): String {
        return try {
            val geocoder = Geocoder(this)
            val list = geocoder.getFromLocation(point.latitude, point.longitude, 1)
            list[0].getAddressLine(0)
        } catch(e: IOException) {
            ""
        }
        }

    private fun Double.roundToDecimal(decimals : Int) : Double
    {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return Math.round(this * multiplier) / multiplier
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        p0?:return
        if (p0.sensor == stepSensor) {
            Log.d("STEPCOUNT", "values: ${p0.values.toString()}")
            if(stepsTotal != null) {
                val valueToAdd =  p0.values[0] - stepsTotal!!
                stepCount.text = "Steps: ${stepCount.text.drop(7).toString().toFloat() + valueToAdd}"
            }

            stepsTotal = p0.values[0]
        }

    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    override fun onPause() {
        super.onPause()
        if(stepSensor != null)
            sm.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        if(stepSensor != null)
            sm.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
    }

    private fun enableStepSensor()
    {
        if(stepSensor != null) {
            stepCount.text = "Steps: 0"
            sm.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun disableStepSensor()
    {
        if(stepSensor != null) {
            sm.unregisterListener(this)
            stepsTotal = null
            stepCount.text = ""
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {} // Simply the presence of this alleviates issues.

}
