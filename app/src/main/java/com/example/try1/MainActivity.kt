package com.example.try1

import android.Manifest.permission.*
import android.content.Intent
import android.content.pm.PackageManager.*
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.util.Log.i
import android.view.View
import android.widget.*
import android.widget.Toast.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.*
import androidx.core.content.ContextCompat.*

class MainActivity : AppCompatActivity() {

    // Declare variables
    var myTag1 = "Button"
    var heartRateBtn: Button? = null; var respRateBtn: Button? = null; var symptomsBtn: Button? = null; var uploadSignsBtn: Button? = null
    var respRateText: TextView? = null; var heartRateText: TextView? = null
    var symptomsDb: SignSymptmsDB? = null
    var heartRt = "0"; var respRt = "0"
    var heartRateVal: String? = null
    var currTime = 0f

    // Override onCreate function in AppCompatActivity class
    override fun onCreate(savedInstanceState: Bundle?) {
        i(myTag1, "App Started")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check if the phone has a camera. If yes, does the app have Permission to access camera? if not, then request for permission
        val cameraPresent = checkCamera()
        if (cameraPresent) {
            if (checkSelfPermission(this@MainActivity,
                            CAMERA) == PERMISSION_DENIED) {
                requestPermissions(this, arrayOf(CAMERA), camPerm)
            } else {
                makeText(this@MainActivity, "Camera Ready for use!", LENGTH_SHORT).show()
            }
        } else {
            makeText(this@MainActivity, "This device does not have a camera!", LENGTH_SHORT).show()
        }

        // Breathing Rate
        respRateBtn = findViewById<View>(R.id.RespRate_B) as Button
        respRateBtn!!.setOnClickListener { view ->
            makeText(this@MainActivity, "Measuring Breathing Rate.", LENGTH_LONG).show()
            Accelerometer(view.context, respRateText)
        }
        respRateText = findViewById<View>(R.id.RespRate_txt) as TextView

        // Heart Rate
        heartRateBtn = findViewById<View>(R.id.HrtRate_B) as Button
        heartRateText = findViewById<View>(R.id.HrtRate_Txt) as TextView
        heartRateVal = ""
        if (!cameraPresent) {
            heartRateBtn!!.isEnabled = false // Disable Heart Rate Button if no camera is found in phone
        }
        heartRateBtn!!.setOnClickListener {
            i(myTag1, "Going to Heart Rate Recording Page")
            val intent = Intent(this@MainActivity, SecondActivity::class.java)
            startActivityForResult(intent, r1) // Go to SecondActivity
        }

        // Create DB and send Heart rate and Respiratory Rate
        uploadSignsBtn = findViewById<View>(R.id.UpldSign_B) as Button
        uploadSignsBtn!!.setOnClickListener {
            i(myTag1, "Upload Signs to database")
            symptomsDb = SignSymptmsDB(applicationContext)
            symptomsDb!!.onCreate(symptomsDb!!.writableDatabase)
            symptomsDb!!.setHeartRateVal(heartRt)
            symptomsDb!!.setRespRateVal(respRt)
        }

        // Symptoms as user input
        symptomsBtn = findViewById<View>(R.id.Symptm_B) as Button
        symptomsBtn!!.setOnClickListener {
            i(myTag1, "Going to Symptoms Logging Page")
            val intent = Intent(this, SymptomsLog::class.java)
            intent.putExtra("HEART_RATE_VALUE_MAIN", heartRateVal)
            intent.putExtra("RESP_RATE_VALUE_MAIN", respRateText!!.text.toString())
            startActivity(intent) // Go to SymptomActivity
        }

    }

    override fun onPause() {
        super.onPause()
        val accel = Accelerometer(applicationContext, respRateText)
        accel.sensorUnregister()
    }

    private fun checkCamera(): Boolean {
        var numOfCameras = 0
        try {
            numOfCameras = (getSystemService(CAMERA_SERVICE) as CameraManager).cameraIdList.size
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        return numOfCameras > 0
    }

    override fun onActivityResult(rqCode: Int, resCode: Int, data: Intent?) {
        super.onActivityResult(rqCode, resCode, data)
        if (rqCode == capVid) {
            if (resCode == RESULT_OK) {
                makeText(this, """
     Video saved to:
     ${data!!.data}
     """.trimIndent(), LENGTH_LONG).show()
            } else if (resCode == RESULT_CANCELED) {
                makeText(this, "Cancelled Video Recording.",
                        LENGTH_LONG).show()
            } else {
                makeText(this, "Video Recoding failed",
                        LENGTH_LONG).show()
            }
        }
        if (rqCode == r1 && resCode == r2) {
            if (data!!.hasExtra("HEART_RATE_VALUE")) {
                heartRateVal = data.getStringExtra("HEART_RATE_VALUE")
                i(myTag1, "onActivityResult: $heartRateVal")
                makeText(this, "Value : $heartRateVal",
                        LENGTH_LONG).show()
                heartRateText!!.text = heartRateVal
            }
        }
    }

    companion object {
        // Declare Static values
        private const val camPerm = 200
        private const val capVid = 101
        private const val r1 = 9999
        private const val r2 = 7777
    }
}