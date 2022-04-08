package com.example.routetracker.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class StepSensor(context: Context?) : SensorEventListener {
    private val sm: SensorManager =
        context?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val sensor: Sensor? = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    var onTriggered: (SensorEvent) -> Unit = {}
    var onStarted: () -> Unit = {}
    var onStopped: () -> Unit = {}

    override fun onSensorChanged(p0: SensorEvent?) {
        if (p0 != null) {
            onTriggered(p0)
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    fun enable() {
        if (sensor != null)
            sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        onStarted()
    }

    fun disable() {
        if (sensor != null)
            sm.unregisterListener(this)
        onStopped()
    }

}