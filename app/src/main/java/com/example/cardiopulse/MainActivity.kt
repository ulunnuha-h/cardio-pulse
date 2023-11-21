package com.example.cardiopulse

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.example.cardiopulse.ViewModel.Device

class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val device = ViewModelProvider(this).get(Device::class.java)

        setContentView(R.layout.activity_main)
        val startButton = findViewById<Button>(R.id.startButton)
        val inputButton = findViewById<ImageButton>(R.id.connectButton)
        val batteryLevel = findViewById<TextView>(R.id.textBattery)
        val status = findViewById<TextView>(R.id.textStatus)

        startButton.setOnClickListener {
//            val intent = Intent(this@MainActivity, MeasureActivity::class.java)
//            startActivity(intent)
            setContentView(R.layout.activity_measure)
        }

        device.battery.observe(this) { it ->
            batteryLevel.text = "Battery level : " + it
        }

        device.status.observe(this) {it ->
            status.text = "Status : " +it
        }

        inputButton.setOnClickListener {
            showCustomDialog(device)
        }
    }

    private fun showCustomDialog(device: Device) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.input_dialog)

        val editText: EditText = dialog.findViewById(R.id.editText)
        val buttonConnect: Button = dialog.findViewById(R.id.buttonConnect)

        editText.setText(device.getId())

        buttonConnect.setOnClickListener {
            // Connect button click action
            val text: String = editText.text.toString() // Get text from EditText
            val textId = findViewById<TextView>(R.id.textId);
            textId.setText("ID : "+text);
            device.setId(text)
            device.connectDevice(applicationContext)
            dialog.dismiss() // Dismiss the dialog
        }

        dialog.show()
    }

}