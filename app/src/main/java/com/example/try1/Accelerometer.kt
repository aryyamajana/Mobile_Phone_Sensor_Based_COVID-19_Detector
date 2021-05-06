package com.example.try1

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.Sensor.TYPE_ACCELEROMETER
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_NORMAL
import android.util.Log.i
import android.widget.TextView
import java.lang.Math.pow
import java.util.*

class Accelerometer(private val c: Context, private val respRateText: TextView?) : SensorEventListener {
    var myTag3 = "Button3"
    private var sensorAccManager: SensorManager? = null
    private var acc: Sensor? = null
    var flag = false
    var startTime = 0f
    var timeDuration = 45
    var zAcc = Vector<Double>()
    private fun sensorInit() {
        sensorAccManager = c.getSystemService(SENSOR_SERVICE) as SensorManager
        acc = sensorAccManager!!.getDefaultSensor(TYPE_ACCELEROMETER)
        sensorAccManager!!.registerListener(this, acc, SENSOR_DELAY_NORMAL)
    }

    fun sensorUnregister() {
        sensorAccManager!!.unregisterListener(this)
    }

    override fun onAccuracyChanged(s: Sensor, accuracy: Int) {}

    override fun onSensorChanged(sE: SensorEvent) {
        val sensorInUse = sE.sensor
        if (sensorInUse.type == TYPE_ACCELEROMETER) {
            if (!flag) {
                flag = true
                startTime = (sE.timestamp/ pow(10.0, 9.0)).toFloat()
            }
            val timeDiff = ((sE.timestamp/ pow(10.0, 9.0)).toFloat()-startTime).toInt()
            if (timeDiff <= timeDuration) {
                i(myTag3, "Time elapsed: $timeDiff seconds")
                val az = sE.values[2].toDouble()
                zAcc.add(az)
            } else {
                sensorUnregister()
                calcRespRate(zAcc)
            }
        }
    }

    var breatheRt = 0
    @SuppressLint("SetTextI18n")
    fun calcRespRate(accZ: Vector<Double>) {
        val corrected = Vector<Double>()
        val temp = Vector<Double>()
        run {
            var it = accZ.size % 42
            while (it + 42 < accZ.size) {
                var s = 0.0
                for (j in it until 42 + it) {
                    s += accZ.elementAt(j)
                }
                corrected.add(s / 42)
                it += 9
            }
        }
        for (i in 1 until corrected.size) {
            temp.add(corrected[i] - corrected[i - 1])
        }
        for (j in 1 until temp.size) {
            val c1 = temp[j].compareTo(0)
            val c2 = temp[j-1].compareTo(0)
            if (c1==0 || c1*c2 < 0) {
                breatheRt++
            }
        }
        breatheRt = (breatheRt/2) * 60 / timeDuration
        i(myTag3, "Calculated respiratory rate is: $breatheRt")
        respRateText!!.text = breatheRt.toString() + ""
    }

    init {
        sensorInit() // Sensor Initialization Constructor
    }
}