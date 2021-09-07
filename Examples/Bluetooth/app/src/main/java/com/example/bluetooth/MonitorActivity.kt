package com.example.bluetooth

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries

class MonitorActivity: Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monitor)

        val data = intent.getIntArrayExtra("data")

        val graph = findViewById<GraphView>(R.id.graph)

        if(data != null) {

            var datapoints = Array<DataPoint>(data.size) {
                DataPoint(it.toDouble(), data[it].toDouble())
            }

            graph.addSeries(LineGraphSeries<DataPoint>(datapoints))
        }
    }
}