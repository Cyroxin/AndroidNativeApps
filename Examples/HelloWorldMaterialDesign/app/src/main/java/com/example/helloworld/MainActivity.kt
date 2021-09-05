package com.example.helloworld

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


// Banner image source: https://cleanpublicdomain.com/
// Uploader: https://cleanpublicdomain.com/vendor-dashboard/prashu8055/

const val CHANNEL_ID = "defaultchannel"

class MainActivity : AppCompatActivity() {
    var layout : ConstraintLayout? = null
    lateinit var takePicture: ActivityResultLauncher<Void>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createNotificationChannel()

        val actionBar: ActionBar? = supportActionBar
        actionBar?.setHomeAsUpIndicator(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        actionBar?.setHomeButtonEnabled(true)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        takePicture = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            if(bitmap != null) {
                findViewById<CardView>(R.id.cardView).foreground =
                    BitmapDrawable(resources, bitmap);

                val fileName = "temp_image"
                val imgPath = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                var imageFile: File? = null
                imageFile = File.createTempFile(fileName, ".jpg", imgPath)

                val os: OutputStream = BufferedOutputStream(FileOutputStream(imageFile))
                bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, os)
                os.close()
            }
        }

        layout = findViewById<ConstraintLayout>(R.id.mainlayout)

        val btn = findViewById<FloatingActionButton>(R.id.send)
        val editbox = findViewById<EditText>(R.id.editbox)
        var repeats : Int = 0 // If user is repeatedly doing the same action

        btn.setOnClickListener {
            if(editbox.text.isEmpty()) {
                if (actionBar?.title == "Hello World") {
                    actionBar?.title = "Goodbye Summer!"
                    if(repeats > 2)
                    {
                        Toast.makeText(applicationContext, "Stop it", Toast.LENGTH_SHORT).show()
                        repeats = 0
                    }
                }
                else {
                    actionBar?.title = "Hello World"
                    repeats++

                }
            }
            else {
                actionBar?.title = "Hello " + editbox.text + '!'
                editbox.text.clear()
                repeats = 0
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {



        if(item.title == "like")
        {
            // when you want to send the notification
            val notif = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.likered)
            .setContentTitle("Back at ya")
            .setContentText("I like you too!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

            NotificationManagerCompat.from(this).notify(1, notif)
        }
        else if(item.title == "Camera")
            takePicture.launch(null)
        else
            Snackbar.make(layout!!,"This does not do anything.", Snackbar.LENGTH_SHORT)
                .setBackgroundTint(Color.WHITE)
                .setTextColor(Color.BLACK)
                .show()



        return super.onOptionsItemSelected(item)

    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
             val channel = NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT)
                 .apply {
                description = getString(R.string.app_name)
            }

            // Register the channel with the system
            val notificationManager: NotificationManager =
                 getSystemService(Context.NOTIFICATION_SERVICE) as
                        NotificationManager
             notificationManager.createNotificationChannel(channel)
            }
        }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.items, menu)
        return super.onCreateOptionsMenu(menu)
    }
}