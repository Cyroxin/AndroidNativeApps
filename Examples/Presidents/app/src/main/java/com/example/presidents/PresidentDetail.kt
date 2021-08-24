package com.example.presidents

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView


class PresidentDetail : Fragment() {

    var name : String? = null
    var startYear : String? = null
    var endYear : String? = null
    var fact : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_president_detail, container, false)

        view?.findViewById<TextView>(R.id.name)?.text = name;
        view?.findViewById<TextView>(R.id.year)?.text = "$startYear - $endYear"
        view?.findViewById<TextView>(R.id.fact)?.text = fact;

        return view
    }

}