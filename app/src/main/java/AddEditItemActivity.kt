package com.example.webrequestchecker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class AddEditItemActivity : AppCompatActivity() {

    private lateinit var inputMintInput: EditText
    private lateinit var outputMintInput: EditText
    private lateinit var inAmountInput: EditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_item)

        inputMintInput = findViewById(R.id.inputMintInput)
        outputMintInput = findViewById(R.id.outputMintInput)
        inAmountInput = findViewById(R.id.inAmountInput)
        saveButton = findViewById(R.id.saveButton)

        val isEditMode = intent.getIntExtra("position", -1) != -1
        if (isEditMode) {
            inputMintInput.setText(intent.getStringExtra("inputMint"))
            outputMintInput.setText(intent.getStringExtra("outputMint"))
            inAmountInput.setText(intent.getStringExtra("inAmount"))
        }

        saveButton.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("inputMint", inputMintInput.text.toString())
            resultIntent.putExtra("outputMint", outputMintInput.text.toString())
            resultIntent.putExtra("inAmount", inAmountInput.text.toString())
            if (isEditMode) {
                resultIntent.putExtra("position", intent.getIntExtra("position", -1))
                setResult(RESULT_OK, resultIntent)
            } else {
                setResult(RESULT_OK, resultIntent)
            }
            finish()
        }
    }
}