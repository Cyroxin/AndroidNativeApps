package com.example.helloworld

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val title = findViewById<TextView>(R.id.textView)
        val btn = findViewById<FloatingActionButton>(R.id.send)
        val editbox = findViewById<EditText>(R.id.editbox)
        btn.setOnClickListener { if(editbox.text.isEmpty()) { if (title.text == "Hello World") title.text = "Goodbye Summer!" else title.text = "Hello World"} else {title.text = "Hello " + editbox.text + '!'; editbox.text.clear()} }

    }
}