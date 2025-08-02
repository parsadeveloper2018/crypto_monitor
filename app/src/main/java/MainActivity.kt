package com.example.webrequestchecker

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val itemList = mutableListOf<Item>()
    private lateinit var adapter: ItemAdapter
    private lateinit var mediaPlayer: MediaPlayer
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false
    private lateinit var monitoringUrl: String
    private var alarmSoundUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Load settings
        val sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        monitoringUrl = sharedPreferences.getString("monitoringUrl", "") ?: ""
        val alarmSoundUriString = sharedPreferences.getString("alarmSoundUri", null)
        alarmSoundUri = if (alarmSoundUriString != null) Uri.parse(alarmSoundUriString) else null

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        adapter = ItemAdapter(itemList, this::onEdit, this::onDelete)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Add new item button
        val addItemButton = findViewById<Button>(R.id.addItemButton)
        addItemButton.setOnClickListener {
            val intent = Intent(this, AddEditItemActivity::class.java)
            startActivityForResult(intent, ADD_ITEM_REQUEST)
        }
    }

    private fun startMonitoring() {
        if (isRunning) return
        isRunning = true

        handler.postDelayed(object : Runnable {
            override fun run() {
                if (isRunning) {
                    fetchWebData()
                    handler.postDelayed(this, 5000) // Check every 5 seconds
                }
            }
        }, 5000)
    }

    private fun stopMonitoring() {
        isRunning = false
    }

    private fun fetchWebData() {
        val client = OkHttpClient()

        for ((index, item) in itemList.withIndex()) {
            val urlBuilder = HttpUrl.Builder()
                .scheme("https")
                .host(monitoringUrl)
                .addPathSegment("api")
                .addQueryParameter("inputMint", item.inputMint)
                .addQueryParameter("outputMint", item.outputMint)
                .addQueryParameter("inAmount", item.inAmount)
                .build()

            val request = Request.Builder().url(urlBuilder).build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Request failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        val jsonResponse = Gson().fromJson(responseBody, WebResponse::class.java)

                        val outAmount = jsonResponse.outAmount.toLong()
                        val threshold = 1000L // Set your threshold here

                        if (outAmount > threshold && item.alarmTime == null) {
                            runOnUiThread {
                                playAlarmSound()
                                item.alarmTime = System.currentTimeMillis().toString()
                                adapter.notifyItemChanged(index)
                                Toast.makeText(this@MainActivity, "Condition met: outAmount = $outAmount", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "Request failed: ${response.code}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }
    }

    private fun playAlarmSound() {
        if (alarmSoundUri != null) {
            mediaPlayer = MediaPlayer.create(this, alarmSoundUri)
            mediaPlayer?.start()
        } else {
            Toast.makeText(this, "No alarm sound selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onEdit(position: Int) {
        val intent = Intent(this, AddEditItemActivity::class.java)
        intent.putExtra("inputMint", itemList[position].inputMint)
        intent.putExtra("outputMint", itemList[position].outputMint)
        intent.putExtra("inAmount", itemList[position].inAmount)
        intent.putExtra("position", position)
        startActivityForResult(intent, EDIT_ITEM_REQUEST)
    }

    private fun onDelete(position: Int) {
        itemList.removeAt(position)
        adapter.notifyItemRemoved(position)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            when (requestCode) {
                ADD_ITEM_REQUEST -> {
                    val inputMint = data.getStringExtra("inputMint")!!
                    val outputMint = data.getStringExtra("outputMint")!!
                    val inAmount = data.getStringExtra("inAmount")!!
                    val newItem = Item(inputMint, outputMint, inAmount)
                    itemList.add(newItem)
                    adapter.notifyItemInserted(itemList.size - 1)
                }
                EDIT_ITEM_REQUEST -> {
                    val position = data.getIntExtra("position", -1)
                    if (position != -1) {
                        val inputMint = data.getStringExtra("inputMint")!!
                        val outputMint = data.getStringExtra("outputMint")!!
                        val inAmount = data.getStringExtra("inAmount")!!
                        itemList[position] = Item(inputMint, outputMint, inAmount)
                        adapter.notifyItemChanged(position)
                    }
                }
            }
        }
    }

    companion object {
        const val ADD_ITEM_REQUEST = 1
        const val EDIT_ITEM_REQUEST = 2
    }
}

data class Item(
    val inputMint: String,
    val outputMint: String,
    val inAmount: String,
    var alarmTime: String? = null
)

data class WebResponse(
    val mode: String,
    val swapMode: String,
    val inputMint: String,
    val outputMint: String,
    val inAmount: String,
    val outAmount: String,
    val otherAmountThreshold: String,
    val slippageBps: Int,
    val priceImpactPct: String
)