package com.example.try1

import android.R.layout.*
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log.*
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Toast.*
import androidx.appcompat.app.AppCompatActivity

@Suppress("ControlFlowWithEmptyBody")
class SymptomsLog : AppCompatActivity() {
    var myTag4 = "Button4"
    var symptomsDropdown: Spinner? = null
    var covidSymptoms = arrayOf("Nausea", "Headache", "Diarrhea", "Soar Throat", "Fever", "Muscle Ache", "Loss_of Smell or Taste",
            "Cough", "Shortness of Breath", "Feeling tired")
    var indexOfSymptom = 0
    var choice = ""
    var starRating: RatingBar? = null
    var arrRating = IntArray(10) {0}
    var uploadSymptomsButton: Button? = null
    var signSymptmsDB: SignSymptmsDB? = null
    var i: Intent? = null
    var heartBPM: String? = null
    var breatheRt: String? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symptom)
        i(myTag4, "Symptom log page opened")
        signSymptmsDB = SignSymptmsDB(applicationContext)
        i = intent
        if (i!!.hasExtra("RESP_RATE_VALUE_MAIN")) {
            heartBPM = i!!.getStringExtra("RESP_RATE_VALUE_MAIN")
        }
        if (i!!.hasExtra("HEART_RATE_VALUE_MAIN")) {
            breatheRt = i!!.getStringExtra("HEART_RATE_VALUE_MAIN")
        }
        symptomsDropdown = findViewById<View>(R.id.Symptms_DrpDwn) as Spinner
        starRating = findViewById<View>(R.id.StarRtng) as RatingBar
        starRating!!.stepSize = 1f
        uploadSymptomsButton = findViewById<View>(R.id.UpldSymptms_B) as Button

        starRating!!.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == 1) {
                val ratingValues = starRating!!.rating
                for (i in arrRating.indices) {}
                arrRating[indexOfSymptom] = ratingValues.toInt()
                makeText(this@SymptomsLog, " $ratingValues", LENGTH_LONG).show()
            }
            false
        }
        symptomsDropdown!!.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
                starRating!!.rating = 0f
                choice = covidSymptoms[i]
                indexOfSymptom = i
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
        val dropdownAdapter = ArrayAdapter(this, simple_spinner_item, covidSymptoms)
        symptomsDropdown!!.adapter = dropdownAdapter
        uploadSymptomsButton!!.setOnClickListener {
            i(myTag4, "Uploading Symptoms to database")
            signSymptmsDB!!.setHeartRateVal(heartBPM)
            signSymptmsDB!!.setRespRateVal(breatheRt)
            signSymptmsDB!!.insertData(arrRating)
            onBackPressed()
        }
    }
}