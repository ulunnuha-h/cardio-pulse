package com.example.cardiopulse

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cardiopulse.ViewModel.Device
import com.example.cardiopulse.ViewModel.ResultViewModel
import java.util.Date


class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "Polar_MainActivity"
        private const val SHARED_PREFS_KEY = "polar_device_id"
        private const val PERMISSION_REQUEST_CODE = 1
    }

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var resultVM : ResultViewModel

    private val bluetoothOnActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode != Activity.RESULT_OK) {
            Log.w(TAG, "Bluetooth off")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode === 335 && resultCode === RESULT_OK) {
            if (data != null) {
                val min: String? = data.getStringExtra("min")
                val avg: String? = data.getStringExtra("avg")
                val max: String? = data.getStringExtra("max")
                val type: String? = data.getStringExtra("type")
                val status: String? = data.getStringExtra("status")
                if (status != null && type != null) {
                    resultVM.addResult(min, avg, max, status, type)
                }
            }
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val device = ViewModelProvider(this).get(Device::class.java)
        resultVM= ViewModelProvider(this).get(ResultViewModel::class.java)
        val resultAdapter = ResultAdapter(resultVM.getResults());
        resultVM.getResultLiveData().observe(this, {resultAdapter.notifyDataSetChanged()})

        val rvResult = findViewById<RecyclerView>(R.id.rvResult)
        rvResult.adapter = resultAdapter
        rvResult.layoutManager = LinearLayoutManager(this);

        val startButton = findViewById<Button>(R.id.startButton)
        val inputButton = findViewById<ImageButton>(R.id.connectButton)
        val batteryLevel = findViewById<TextView>(R.id.textBattery)
        val status = findViewById<TextView>(R.id.textStatus)
        val ageInput = findViewById<EditText>(R.id.ageInput)

        device.battery.observe(this){ it ->
            batteryLevel.setText("Battery level : "+ it);
        }

        device.status.observe(this){it ->
            status.setText("Status : "+it);
        }

        startButton.setOnClickListener {
            if(ageInput.text.toString().equals("")) {
                showToast("Please fill in the age input");
            } else {
                checkBT()
                if (device.getId() == null || device.getId() == "") {
                    val textId = findViewById<TextView>(R.id.textId);
                    textId.setText("ID : 1234567")
                    device.setId("1234567")

                } else {
                    showToast(getString(R.string.connecting) + " " + device.getId())
                    val intent = Intent(this, HRActivity::class.java)
                    intent.putExtra("id", device.getId())
                    intent.putExtra("age", ageInput.text.toString())
                    startActivityForResult(intent, 335)
                }
            }
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
            val text: String = editText.text.toString()
            val textId = findViewById<TextView>(R.id.textId);
            textId.setText("ID : "+text);
            device.setId(text)
            checkBT()
            device.connectDevice(this@MainActivity)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun checkBT() {
        showToast("Checking Bluetooth")
        val btManager = applicationContext.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter: BluetoothAdapter? = btManager.adapter
        if (bluetoothAdapter == null) {
            showToast("Device doesn't support Bluetooth")
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

            bluetoothOnActivityResultLauncher.launch(enableBtIntent)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT), PERMISSION_REQUEST_CODE)
            } else {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
            }
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_REQUEST_CODE)
        }
    }

    private fun showToast(message: String) {
        val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_LONG)
        toast.show()
    }
}
