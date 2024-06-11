package com.example.thebendsver2

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.thebendsver2.utils.FFT
@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {
    private lateinit var frequencyTextView: TextView
    private lateinit var checkText: TextView
    private lateinit var startButton: Button
    private lateinit var notesSpinner: Spinner
    private lateinit var bendSpinner: Spinner
    private var isRecording = false

    val mutableMap = mutableMapOf("F" to 349, "F#" to 369,
        "G" to 392, "G#" to 415, "A" to 440, "A#" to 466, "B" to 493,
        "C" to 523, "C#" to 554, "C#" to 587, "E" to 622, "F13" to 659, "F#14" to 698)

    private val notes = arrayOf("F", "F#", "G", "G#", "A", "A#", "B", "C", "C#", "E")
    private val bends = arrayOf("1/2", "Full")

    private var rightFrequency = 0
    private var rightNoteHalf = "E"
    private var rightNoteFull = "E"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        frequencyTextView = findViewById(R.id.frequencyTextView)
        checkText = findViewById(R.id.textCheck)
        startButton = findViewById(R.id.startButton)

        val notesAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, notes)
        val bendsAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bends)

        notesSpinner = findViewById(R.id.spinnerNotes)
        bendSpinner = findViewById(R.id.spinnerBend)

        notesSpinner.adapter = notesAdapter
        bendSpinner.adapter = bendsAdapter

        notesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?,
                                        position: Int, id: Long) {
                rightNoteHalf = notesAdapter.getItem(position + 1).toString()
                rightNoteFull = notesAdapter.getItem(position + 2).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        bendSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?,
                                        position: Int, id: Long) {
                if (bendsAdapter.getItem(position).equals("1/2")) {
                    rightFrequency = mutableMap[rightNoteHalf]!!
                } else if (bendsAdapter.getItem(position).equals("Full")) {
                    rightFrequency = mutableMap[rightNoteFull]!!
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

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
                    if (rightFrequency <= frequency) {
                        checkText.setTextColor(Color.parseColor("#008000"))
                        checkText.text = "Right"
                    } else {
                        checkText.setTextColor(Color.parseColor("#FF0000"))
                        checkText.text = "Wrong"
                    }
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