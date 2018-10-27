package com.edgardo.corasensor.database

import android.arch.persistence.room.TypeConverters
import android.graphics.Bitmap
import android.content.Context
import android.graphics.BitmapFactory
import com.edgardo.corasensor.helpers.Converters
import com.edgardo.corasensor.Scan.Scan

@TypeConverters(Converters::class)
class ScanDataTest (var context: Context ) {
    val scanList: MutableList<Scan> = ArrayList()


    private fun getBitmap(imageId:Int): Bitmap = BitmapFactory.decodeResource(context.resources,imageId)

    init {
        dataList()
    }

    fun dataList(){
        scanList.add(Scan(pressureAvg = 120.0, pressureSystolic = 100.0, pressureDiastolic = 80.0, scanDate = "25/10/2018"))
        scanList.add(Scan(pressureAvg = 120.0, pressureSystolic = 100.0, pressureDiastolic = 80.0, scanDate = "25/10/2018"))
        scanList.add(Scan(pressureAvg = 120.0, pressureSystolic = 100.0, pressureDiastolic = 80.0, scanDate = "25/10/2018"))
    }
}


