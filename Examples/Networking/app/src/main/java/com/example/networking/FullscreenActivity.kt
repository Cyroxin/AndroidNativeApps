package com.example.networking

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.SystemClock.sleep
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.networking.helpers.Conn
import com.example.networking.helpers.downloadtype
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
@Suppress("DeferredResultUnused")
class FullscreenActivity : AppCompatActivity() {


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    val url_sampledata = "https://github.com/Cyroxin/AndroidStudioExamples/raw/main/Examples/Networking/app/sampledata/"
    val url_description = url_sampledata + "description.txt"
    val url_emoji = url_sampledata + "emoji.png"

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen)
        supportActionBar?.hide()

        val content = findViewById<TextView>(R.id.content)
        val overlay = findViewById<ImageView>(R.id.overlay)

        content.visibility = View.GONE

        content.setOnClickListener { toggleContent(content,overlay) }
        overlay.setOnClickListener { toggleContent(content,overlay) }

        if (!fetch()) {
            findViewById<TextView>(R.id.desc).text =
                "Cannot show content as there is no network available. Restart the app once it is available."

            CoroutineScope(Dispatchers.IO).async {
                while (!fetch()){ sleep(1000)}
            }
        }

    }

    // UI

    private fun toggleContent(content : View, overlay : View)
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

    // Networking

    private fun fetch() : Boolean
    {
        if (isNetworkAvailable()) {

            // Fetch content
            CoroutineScope(Dispatchers.IO).async {
                Conn(mHandler, url_emoji, downloadtype.image, 1).run()


                // Fetch description
                val myRunnable = Conn(mHandler, url_description, downloadtype.string, 0)
                val myThread = Thread(myRunnable)
                myThread.start()
            }
            return true
        }
        else
            return false
    }

    private fun isNetworkAvailable(): Boolean =
        (this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).isDefaultNetworkActive

    private val mHandler: Handler = object :
        Handler(Looper.getMainLooper()) {
        override fun handleMessage(inputMessage: Message) {
            if (inputMessage.what == 0) {
                // Description
                val text = inputMessage.obj.toString()
                findViewById<TextView>(R.id.desc).text = text
            }
            else if (inputMessage.what == 1)
            {
                // Emoji
                findViewById<TextView>(R.id.content).foreground = BitmapDrawable(resources, inputMessage.obj as Bitmap)

            }
        }
    }

}