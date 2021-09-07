package com.example.notepad.ui.main

import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.example.notepad.R
import java.io.File

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.main_fragment, container, false)

        view.findViewById<Button>(R.id.save).setOnClickListener {
            val edittext = view.findViewById<EditText>(R.id.edit)

            if(Environment.getExternalStorageState() ==
                Environment.MEDIA_MOUNTED) {
                    val file = File(context?.getExternalFilesDir(null),
                        "notepad.txt")
                file.writeText("${edittext.text}\n")
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.container, SecondFragment.newInstance(edittext.text.toString()))
                    .addToBackStack("fragment")
                    .commit()
                edittext.text.clear()
            }
        }

        return view
    }


}