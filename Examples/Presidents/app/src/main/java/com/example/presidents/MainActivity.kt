package com.example.presidents

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock.sleep
import android.util.Log
import androidx.fragment.app.FragmentManager
import android.view.View
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView

class MainActivity : AppCompatActivity() {

    private val tag : String = "PresidentsApp"

    private val listFragment = ListFragment()
    private val detailFragment = PresidentDetail()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        // Set Click Handler
        listFragment.onClick = { president ->
            run {
                Log.i("ClickedOn", president.name)

                detailFragment.name = president.name
                detailFragment.startYear = president.StartYear.toString()
                detailFragment.endYear = president.EndYear.toString()
                detailFragment.fact = president.fact

                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerView, detailFragment)
                    .setReorderingAllowed(true)
                    .addToBackStack(null)
                    .commit();
            }
        }

        // Navigate
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainerView, listFragment)
            .commit();

    }
}