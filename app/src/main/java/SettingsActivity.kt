package com.example.webrequestchecker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var comboBoxUrlInput: EditText
    private lateinit var monitoringUrlInput: EditText
    private lateinit var selectSoundButton: Button
    private lateinit var saveSettingsButton: Button

    private var selectedSoundUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        comboBoxUrlInput = findViewById(R.id.comboBoxUrlInput)
        monitoringUrlInput = findViewById(R.id.monitoringUrlInput)
        selectSoundButton = findViewById(R.id.selectSoundButton)
        saveSettingsButton = findViewById(R.id.saveSettingsButton)

        // Load saved settings
        val sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        comboBoxUrlInput.setText(sharedPreferences.getString("comboBoxUrl", ""))
        monitoringUrlInput.setText(sharedPreferences.getString("monitoringUrl", ""))

        // Select sound file
        selectSoundButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "audio/*"
            startActivityForResult(intent, SELECT_SOUND_REQUEST)
        }

        // Save settings
        saveSettingsButton.setOnClickListener {
            val comboBoxUrl = comboBoxUrlInput.text.toString()
            val monitoringUrl = monitoringUrlInput.text.toString()

            if (comboBoxUrl.isNotEmpty() && monitoringUrl.isNotEmpty()) {
                val editor = sharedPreferences.edit()
                editor.putString("comboBoxUrl", comboBoxUrl)
                editor.putString("monitoringUrl", monitoringUrl)
                editor.putString("alarmSoundUri", selectedSoundUri?.toString())
                editor.apply()

                Toast.makeText(this, "Settings saved successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_SOUND_REQUEST && resultCode == RESULT_OK) {
            selectedSoundUri = data?.data
            Toast.makeText(this, "Sound selected", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val SELECT_SOUND_REQUEST = 1
    }
}