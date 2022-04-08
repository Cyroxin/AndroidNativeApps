package com.example.routetracker

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.routetracker.helpers.fetchPointsOfInterest
import com.example.routetracker.sensors.StepSensor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.util.BoundingBox


class DashboardFragment(private var mapFragment: MapFragment) : Fragment() {

    companion object {
        fun newInstance(mapFragment: MapFragment) = DashboardFragment(mapFragment)
    }

    var stepSensor: StepSensor = StepSensor(mapFragment.requireContext())
    private var stepStartCount: Float = 0f
    private var stepEndCount: Float = 0f

    private var loading = false


    /* UI */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        Log.e("Construct", "Now")

        view.findViewById<Button>(R.id.cardView1).also { it.setOnClickListener { onClick(it) } }
        view.findViewById<Button>(R.id.cardView2).also { it.setOnClickListener { onClick(it) } }
        view.findViewById<Button>(R.id.cardView3).also { it.setOnClickListener { onClick(it) } }
        view.findViewById<Button>(R.id.cardView4).also { it.setOnClickListener { onClick(it) } }
        view.findViewById<Button>(R.id.cardView5).also { it.setOnClickListener { onClick(it) } }
        view.findViewById<Button>(R.id.cardView6).also { it.setOnClickListener { onClick(it) } }

        // Smart Content
        val notificationlist = view.findViewById<LinearLayout>(R.id.notificationlist)

        // Stepcounter

        val stepview = createSmartCard(
            "Steps",
            (stepEndCount - stepStartCount).toInt().toString()
        )
        stepview.visibility = View.INVISIBLE
        notificationlist.addView(stepview)

        stepSensor.onTriggered = {
            Log.d("Steps", it.values[0].toString())
            if (stepStartCount == 0f)
                stepStartCount = it.values[0]
            else if (stepStartCount != it.values[0]) {
                stepEndCount = it.values[0]
                stepview.findViewById<TextView>(R.id.widecardsubtitle).text =
                    (stepEndCount - stepStartCount).toInt().toString()

                if (!stepview.isVisible && stepEndCount != stepStartCount) stepview.visibility =
                    View.VISIBLE
            }
            Log.e("StepcountStart", stepStartCount.toString())
            Log.e("StepcountEnd", stepEndCount.toString())
        }
        stepSensor.enable()


        fetchPointsOfInterest("Attractions", mapFragment.map.boundingBox) { pois ->
            val cards = mutableListOf<View>()
            pois.forEach { poi ->
                cards.add(createSmartCard(
                    "Nearby",
                    poi.mDescription.takeWhile { character -> character != ',' }).also {
                    it.setOnClickListener {
                        parentFragmentManager.popBackStack()
                        mapFragment.panning = true

                        mapFragment.map.zoomToBoundingBox(
                            BoundingBox.fromGeoPoints(
                                mutableListOf(poi.mLocation)
                            ).increaseByScale(0.00001f), true
                        )

                        mapFragment.pois.add(poi)
                        mapFragment.createPointsOfInterest()
                    }
                })
            }

            CoroutineScope(Dispatchers.Main).launch {
                cards.forEach { notificationlist.addView(it) }
            }

        }


        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stepSensor.disable()
    }

    private fun createSmartCard(title: String, subtitle: String): View {
        val v = LayoutInflater.from(context).inflate(R.layout.view_widecard, null).also {
            if (it.tag != null) it.setOnClickListener { onClick(it) }
        }
        val titleTextView = v.findViewById<TextView>(R.id.widecardtitle)
        val subtitleTextView = v.findViewById<TextView>(R.id.widecardsubtitle)

        titleTextView.text = title
        subtitleTextView.text = subtitle
        return v
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onPause() {
        super.onPause()
        Log.e("Pause", "Paused")
        stepSensor.disable()
        val sharedPreferences = this.requireActivity()
            .getSharedPreferences("pref", Context.MODE_PRIVATE)
        var editor = sharedPreferences.edit()
        editor.putFloat("stepcounterstart", stepStartCount)
        editor.putFloat("stepcounterend", stepEndCount)
        editor.commit()
    }

    override fun onResume() {
        Log.e("Resume", "Resumed")
        super.onResume()
        stepSensor.enable()
        val sharedPreferences = this.requireActivity()
            .getSharedPreferences("pref", Context.MODE_PRIVATE)
        stepStartCount = sharedPreferences.getFloat("stepcounterstart", 0f)
        stepEndCount = sharedPreferences.getFloat("stepcounterend", 0f)
    }

    private fun onClick(view: View) {
        if (loading) return
        loading = true
        fetchPointsOfInterest(view.tag as String, mapFragment.map.boundingBox) {
            CoroutineScope(Dispatchers.Main).launch {
                parentFragmentManager?.popBackStackImmediate()
            }
            mapFragment.pois = it.toMutableList()
            mapFragment.createPointsOfInterest()
        }
    }
}
