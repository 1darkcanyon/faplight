package com.example.shaketorch

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var cameraManager: CameraManager
    private lateinit var cameraId: String
    private lateinit var status: TextView

    private var torchOn = false
    private var lastShake = 0L

    // Tune these: higher threshold = harder shake needed
    private val shakeThreshold = 14f   // m/s^2 above gravity
    private val cooldownMs = 1000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        status = TextView(this).apply {
            textSize = 24f
            text = "Shake to toggle flashlight"
            setPadding(48, 200, 48, 48)
        }
        setContentView(status)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraId = cameraManager.cameraIdList.first()
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_UI
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(e: SensorEvent) {
        val (x, y, z) = e.values
        val g = sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH
        val now = System.currentTimeMillis()
        if (g > shakeThreshold && now - lastShake > cooldownMs) {
            lastShake = now
            toggleTorch()
        }
    }

    private fun toggleTorch() {
        torchOn = !torchOn
        cameraManager.setTorchMode(cameraId, torchOn)
        status.text = if (torchOn) "Flashlight ON" else "Flashlight OFF"
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
