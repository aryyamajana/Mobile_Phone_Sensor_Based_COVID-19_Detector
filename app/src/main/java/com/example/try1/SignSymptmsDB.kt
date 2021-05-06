package com.example.try1

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log

class SignSymptmsDB(context: Context?) : SQLiteOpenHelper(context, DbName, null, DbVer), BaseColumns {
    var myTag5 = "DB"
    private var breatheRt: String? = "0"
    private var heartBPM: String? = "0"
    override fun onCreate(db: SQLiteDatabase) {
        Log.i(myTag5, "Database created")
        if (db.path != null) {
            db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + TableName + "'", null).use { cursor ->
                if (cursor != null) {
                    if (cursor.count <= 0) { // DB does not exist
                        db.execSQL("CREATE TABLE " + TableName + " (" + BaseColumns._ID + " INTEGER PRIMARY KEY, " + col1 + " TEXT, " + col2 + " TEXT, "
                                + col3 + " INT, " + col4 + " INT, " + col5 + " INT, " + col6 + " INT, "
                                + col7 + " INT, " + col8 + " INT, " + col9 + " INT, " + col10 + " INT, "
                                + col11 + " INT, " + col12 + " INT)") // create a new DB
                    }
                }
            }
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVer: Int, newVer: Int) {
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVer: Int, newVer: Int) {
        onUpgrade(db, oldVer, newVer)
    }

    fun setHeartRateVal(heartRateVal: String?) {
        this.heartBPM = heartRateVal
    }

    fun setRespRateVal(respRateVal: String?) {
        this.breatheRt = respRateVal
    }

    fun insertData(arrRating: IntArray): Boolean {
        val db = this.writableDatabase
        val contents = ContentValues()
        contents.put(col1, breatheRt)
        contents.put(col2, heartBPM)
        contents.put(col3, arrRating[0])
        contents.put(col4, arrRating[1])
        contents.put(col5, arrRating[2])
        contents.put(col6, arrRating[3])
        contents.put(col7, arrRating[4])
        contents.put(col8, arrRating[5])
        contents.put(col9, arrRating[6])
        contents.put(col10, arrRating[7])
        contents.put(col11, arrRating[8])
        contents.put(col12, arrRating[9])
        Log.i("Stored values", contents.toString())
        val tableR = db.insert(TableName, null, contents)
        return tableR != -1L
    }

    companion object {
        const val DbName = "Jana.db"
        const val DbVer = 1
        const val TableName = "Covid19_Symptoms"
        const val col1 = "Heart_Rate"
        const val col2 = "Respiratory_Rate"
        const val col3 = "Nausea"
        const val col4 = "Headache"
        const val col5 = "Diarrhea"
        const val col6 = "Soar_Throat"
        const val col7 = "Fever"
        const val col8 = "Muscle_Ache"
        const val col9 = "Loss_of_Smell_or_Taste"
        const val col10 = "Cough"
        const val col11 = "Shortness_of_Breath"
        const val col12 = "Feeling_tired"
    }
}