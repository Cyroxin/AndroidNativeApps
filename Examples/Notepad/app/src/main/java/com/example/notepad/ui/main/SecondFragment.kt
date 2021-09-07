package com.example.notepad.ui.main

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.notepad.R

class SecondFragment(val text: String = "") : Fragment() {

    companion object {
        fun newInstance(text : String) = SecondFragment(text)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.second_fragment, container, false)
        view.findViewById<TextView>(R.id.textView).text = text
        return view
    }


}