package com.example.networking

import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.networking.databinding.ActivityFullscreenBinding
import org.w3c.dom.Text

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenActivity : AppCompatActivity() {


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen)
        supportActionBar?.hide()

        val content = findViewById<TextView>(R.id.fakecontent)
        val overlay = findViewById<ImageView>(R.id.overlaycontent)

        content.visibility = View.GONE

        content.setOnClickListener { toggleContent(content,overlay) }
        overlay.setOnClickListener { toggleContent(content,overlay) }

    }


    fun toggleContent(content : View, overlay : View)
    {
        if (content.visibility == View.GONE) {
            content.visibility = View.VISIBLE
            overlay.visibility = View.GONE
        }
        else
        {
            content.visibility = View.GONE
            overlay.visibility = View.VISIBLE
        }
    }

}