package com.example.cardiopulse

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiCallback
import com.polar.sdk.api.PolarBleApiDefaultImpl
import com.polar.sdk.api.errors.PolarInvalidArgument
import com.polar.sdk.api.model.PolarDeviceInfo
import com.polar.sdk.api.model.PolarHrData
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID


class HRActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "HRActivity"
    }

    private lateinit var api: PolarBleApi
    private lateinit var textViewHR: TextView
    private lateinit var textViewMeanHR: TextView
    private lateinit var textViewMin: TextView
    private lateinit var textViewMax: TextView
    private lateinit var textViewAge: TextView
    private lateinit var textViewStatus: TextView
    private lateinit var date: TextView
    private lateinit var stopButton : Button
    private var hrDisposable: Disposable? = null
    val data = mutableListOf<Int>();
    var min : Int = 200;
    var max : Int = 0;
    var avg : Int = 50;
    var status : String = "Normal";

    private lateinit var deviceId: String
    private var age: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hractivity)
        var dateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
        deviceId = intent.getStringExtra("id") ?: throw Exception("HRActivity couldn't be created, no deviceId given")
        age = Integer.parseInt(intent.getStringExtra("age"))

        textViewHR = findViewById(R.id.HRText)
        textViewMeanHR = findViewById(R.id.HRAvg)
        textViewMin = findViewById(R.id.HRMin)
        textViewMax = findViewById(R.id.HRMax)
        textViewAge = findViewById(R.id.HRAge)
        textViewStatus = findViewById(R.id.HRStatus)
        date = findViewById(R.id.HRDate)

        textViewAge.setText("Age : "+age.toString())
        date.setText(dateFormat.format(Date()))

        stopButton = findViewById(R.id.stopButton)
        stopButton.setOnClickListener {
            val intent = Intent()
            intent.putExtra("min", min.toString())
            intent.putExtra("avg", avg.toString())
            intent.putExtra("max", max.toString())
            intent.putExtra("status", status)
            intent.putExtra("type", "HR")
            setResult(RESULT_OK, intent)
            finish()
        }

        api = PolarBleApiDefaultImpl.defaultImplementation(
            applicationContext,
            setOf(
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_ONLINE_STREAMING,
                PolarBleApi.PolarBleSdkFeature.FEATURE_BATTERY_INFO,
                PolarBleApi.PolarBleSdkFeature.FEATURE_DEVICE_INFO
            )
        )
        api.setApiLogger { str: String -> Log.d("SDK", str) }
        api.setApiCallback(object : PolarBleApiCallback() {
            override fun blePowerStateChanged(powered: Boolean) {
                Log.d(TAG, "BluetoothStateChanged $powered")
            }

            override fun deviceConnected(polarDeviceInfo: PolarDeviceInfo) {
                Log.d(TAG, "Device connected ${polarDeviceInfo.deviceId}")
                Toast.makeText(applicationContext, R.string.connected, Toast.LENGTH_SHORT).show()
            }

            override fun deviceConnecting(polarDeviceInfo: PolarDeviceInfo) {
                Log.d(TAG, "Device connecting ${polarDeviceInfo.deviceId}")
            }

            override fun deviceDisconnected(polarDeviceInfo: PolarDeviceInfo) {
                Log.d(TAG, "Device disconnected ${polarDeviceInfo.deviceId}")
            }

            override fun bleSdkFeatureReady(identifier: String, feature: PolarBleApi.PolarBleSdkFeature) {
                Log.d(TAG, "feature ready $feature")

                when (feature) {
                    PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_ONLINE_STREAMING -> {
                        streamHR()
                    }
                    else -> {}
                }
            }
        })

        try {
            api.connectToDevice(deviceId)
        } catch (a: PolarInvalidArgument) {
            a.printStackTrace()
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        api.shutDown()
    }

    fun streamHR() {
        val isDisposed = hrDisposable?.isDisposed ?: true
        if (isDisposed) {
            hrDisposable = api.startHrStreaming(deviceId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { hrData: PolarHrData ->
                        for (sample in hrData.samples) {
                            Log.d(TAG, "HR ${sample.hr} RR ${sample.rrsMs}")

                            if(sample.hr > 0){
                                min = Math.min(min, sample.hr)
                            }
                            max = Math.max(max, sample.hr)
                            textViewMin.text = min.toString()
                            textViewMax.text = max.toString()
                            textViewHR.text = sample.hr.toString()
                            data.add(sample.hr);
                            if(sample.hr > (220 - age)){
                                status = "Over Max!"
                            } else {
                                status = "Normal"
                            }
                            textViewStatus.text = status;
                            textViewMeanHR.text = (data.sum()/data.count()).toString();
                            avg = data.sum()/data.count()

                        }
                    },
                    { error: Throwable ->
                        Log.e(TAG, "HR stream failed. Reason $error")
                        hrDisposable = null
                    },
                    { Log.d(TAG, "HR stream complete") }
                )
        } else {
            // NOTE stops streaming if it is "running"
            hrDisposable?.dispose()
            hrDisposable = null
        }
    }
}