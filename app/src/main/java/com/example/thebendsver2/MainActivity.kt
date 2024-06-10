package com.example.thebendsver2

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.thebendsver2.utils.FFT

class MainActivity : AppCompatActivity() {
    private lateinit var frequencyTextView: TextView
    private lateinit var startButton: Button
    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        frequencyTextView = findViewById(R.id.frequencyTextView)
        startButton = findViewById(R.id.startButton)

        startButton.setOnClickListener {
            if (isRecording) {
                isRecording = false
                startButton.text = "Start"
            } else {
                isRecording = true
                startButton.text = "Stop"
                startRecording()
            }
        }
    }

    private fun startRecording() {
        // Проверка разрешения на запись аудио
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
            return
        }

        val bufferSize = AudioRecord.getMinBufferSize(44100,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT)

        val audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC,
            44100, AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT, bufferSize)

        audioRecord.startRecording()

        val buffer = ShortArray(bufferSize)
        val thread = Thread {
            while (isRecording) {
                val read = audioRecord.read(buffer, 0, bufferSize)
                val frequency = FFT.calculateFrequency(buffer, read)
                runOnUiThread {
                    frequencyTextView.text = "Frequency: %.2f Hz".format(frequency)
                }
            }
            audioRecord.stop()
            audioRecord.release()
        }
        thread.start()
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Разрешение было предоставлено, можно продолжить запись
                if (isRecording) {
                    startRecording()
                }
            } else {
                // Разрешение было отклонено
                frequencyTextView.text = "Permission denied"
            }
        }
    }
}