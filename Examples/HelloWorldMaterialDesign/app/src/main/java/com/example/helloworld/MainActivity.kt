package com.example.helloworld

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.app.ActionBar
import android.view.Menu

// Banner image source: https://cleanpublicdomain.com/
// Uploader: https://cleanpublicdomain.com/vendor-dashboard/prashu8055/

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val actionBar: ActionBar? = supportActionBar
        actionBar?.setHomeAsUpIndicator(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        actionBar?.setHomeButtonEnabled(true)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        val btn = findViewById<FloatingActionButton>(R.id.send)
        val editbox = findViewById<EditText>(R.id.editbox)
        btn.setOnClickListener { if(editbox.text.isEmpty()) { if (actionBar?.title == "Hello World") actionBar?.title = "Goodbye Summer!" else actionBar?.title = "Hello World"} else {actionBar?.title = "Hello " + editbox.text + '!'; editbox.text.clear()} }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.items, menu)
        return super.onCreateOptionsMenu(menu)
    }
}