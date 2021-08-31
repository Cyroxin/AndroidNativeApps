package com.example.networking.helpers

import android.graphics.BitmapFactory
import android.os.Handler
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

enum class downloadtype
{
    string,
    image
}

class Conn(
    mHand: Handler,
    private val url : String,
    private val type : downloadtype,
    private val what: Int,
    private val request: String? = null) : Runnable {
    private val myHandler = mHand
    override fun run() {
        try {
            val myUrl = URL(url)
            val myConn = myUrl.openConnection()
                    as HttpURLConnection
            myConn.requestMethod = "GET"
            myConn.connectTimeout = 1500


            if(request != null)
            {
                myConn.doOutput = true
                val stream = myConn.outputStream
                stream.bufferedWriter().use {
                    it.write(request)
                }
            }

            val inputStream: InputStream = myConn.inputStream
            val msg = myHandler.obtainMessage()
            msg.what = what

            if(type == downloadtype.string) {
                val allText = inputStream.bufferedReader().use {
                    it.readText()
                }

                val result = StringBuilder()
                result.append(allText)
                val str = result.toString()

                // return
                msg.obj = str
            }
            else
                msg.obj = BitmapFactory.decodeStream(inputStream)

            myHandler.sendMessage(msg)
        } catch (e: Exception) {
            val msg = myHandler.obtainMessage()
            msg.what = -1
            msg.obj = e.message
            myHandler.sendMessage(msg)
        }
    }

}
