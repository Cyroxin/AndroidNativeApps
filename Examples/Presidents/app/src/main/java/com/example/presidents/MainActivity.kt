package com.example.presidents

import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.SystemClock.sleep
import android.util.Log
import android.widget.Toast
import com.example.networking.helpers.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    private val listFragment = ListFragment()
    private val detailFragment = PresidentDetail()

    private var loading : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        // Set Click Handler
        listFragment.onClick = { president ->
            run {
                Log.i("ClickedOn", president.name)

                if(!loading) {
                    loading = !loading

                    val URL =
                        "https://en.wikipedia.org/w/api.php?action=query&format=json&list=search&srsearch=${president.name}"

                    detailFragment.hits = "" // Will be fetched next
                    detailFragment.name = president.name
                    detailFragment.startYear = president.StartYear.toString()
                    detailFragment.endYear = president.EndYear.toString()
                    detailFragment.fact = president.fact

                    if (!fetch(URL)) // If no network
                    {

                        Log.e("Connection", "No connection, retrying...")
                        Toast.makeText(
                            applicationContext,
                            "No connection, retrying...",
                            Toast.LENGTH_SHORT
                        )
                        CoroutineScope(Dispatchers.IO).async {
                            while (!fetch(URL)) {
                                sleep(500); }
                            Log.i("Connection", "Regained connection")
                            Toast.makeText(
                                applicationContext,
                                "Regained connection",
                                Toast.LENGTH_SHORT
                            )
                        }
                    }
                }
            }
        }

        // Navigate
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainerView, listFragment)
            .commit();

    }

    private fun fetch(URL : String) : Boolean
    {
        if (isNetworkAvailable()) {

            // Fetch content
            CoroutineScope(Dispatchers.IO).async {
                Conn(mHandler, URL, downloadtype.string, 0).run()
            }
            return true
        }
        else
            return false
    }

    private fun isNetworkAvailable(): Boolean =
        (this.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager).isDefaultNetworkActive

    private val mHandler: Handler = object :
        Handler(Looper.getMainLooper()) {
        override fun handleMessage(inputMessage: Message) {
            if (inputMessage.what == 0) {
                // Description
                val jsontext = inputMessage.obj.toString()

                Log.i("fetch",jsontext)
                val jsonObj = JSONObject(jsontext.substring(jsontext.indexOf("{"), jsontext.lastIndexOf("}") + 1))

                detailFragment.hits = "Hits: ${((jsonObj["query"] as JSONObject)["searchinfo"] as JSONObject).getInt("totalhits")}";

            }

            // Show the next page regardless if there was an issue or not.
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, detailFragment)
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .commit();
            loading = false
        }
    }
}