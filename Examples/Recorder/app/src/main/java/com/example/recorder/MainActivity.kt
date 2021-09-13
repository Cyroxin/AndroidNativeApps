package com.example.recorder

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.renderscript.ScriptGroup
import android.util.Log
import android.widget.ImageButton
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import java.io.*

class MainActivity : AppCompatActivity() {
    var recRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val record = findViewById<ImageButton>(R.id.btnRecord)
        record.background.setTint(Color.GREEN)
        record.setOnClickListener {
            if (recRunning) {
                record.background.setTint(Color.GREEN)
                CoroutineScope(Dispatchers.IO).async {
                    stopRecording()
                    while (getrecording() == null) { }
                    Log.e("DBG", "Found recording")
                    playback(getrecording()!!)
                }
            } else {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.RECORD_AUDIO
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.d("DBG", "No audio recorder access")
                    requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 1);
                } else {
                    record.background.setTint(Color.RED)
                    CoroutineScope(Dispatchers.IO).async {
                        startRecording()
                    }
                }
            }

        }
    }

    @SuppressLint("MissingPermission")
    private fun startRecording() {

        recRunning = true

        var recFile: File

        val recFileName = "testrecording.raw"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        try {
            recFile = File(storageDir.toString() + "/" + recFileName)

            try {
                val outputStream = FileOutputStream(recFile)
                val bufferedOutputStream = BufferedOutputStream(outputStream)
                val dataOutputStream = DataOutputStream(bufferedOutputStream)

                val minBufferSize = AudioRecord.getMinBufferSize(
                    44100,
                    AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val aFormat = AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(44100)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build()

                val recorder = AudioRecord.Builder()
                    .setAudioSource(MediaRecorder.AudioSource.MIC)
                    .setAudioFormat(aFormat)
                    .setBufferSizeInBytes(minBufferSize)
                    .build()
                val audioData = ByteArray(minBufferSize)
                recorder.startRecording()

                while(recRunning && recorder.read(audioData, 0, minBufferSize) > 0) dataOutputStream.write(audioData)

                recorder.stop()
                dataOutputStream.close()
            } catch (e: IOException) {
                Log.e("FYI", "Recording error $e")
            }
        } catch (ex: IOException) {
            Log.e("FYI", "Can't create audio file $ex")
        }
    }

    private suspend fun stopRecording() {
        delay(1000)
        recRunning = false
    }

    private fun getrecording(): InputStream? {
        val recFileName = "testrecording.raw"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        var playfile: File

        return try {
            playfile = File(storageDir.toString() + "/" + recFileName)
            playfile.inputStream()
        } catch (ex: IOException) {
            Log.e("FYI", "Can't play audio file $ex")
            null
        }
        return null
    }

    private fun playback(istream: InputStream) {
        val minBufferSize = AudioTrack.getMinBufferSize(
            44100, AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val aBuilder = AudioTrack.Builder()
        val aAttr: AudioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        val aFormat: AudioFormat = AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(44100)
            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
            .build()
        val track = aBuilder.setAudioAttributes(aAttr)
            .setAudioFormat(aFormat)
            .setBufferSizeInBytes(minBufferSize)
            .build()
        track!!.setVolume(0.4f)

        track!!.play()
        var i = 0
        val buffer = ByteArray(minBufferSize)
        try {
            i = istream.read(buffer, 0, minBufferSize)
            while (i != -1) {
                track!!.write(buffer, 0, i)
                i = istream.read(buffer, 0, minBufferSize)
            }
        } catch (e: IOException) {
            Log.e("FYI", "Stream read error $e")
        }
        try {
            istream.close()
        } catch (e: IOException) {
            Log.e("FYI", "Close error $e")
        }
        track!!.stop()
        track!!.release()
    }

}