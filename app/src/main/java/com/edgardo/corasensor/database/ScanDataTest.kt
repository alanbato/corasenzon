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
        scanList.add(Scan(brazo = true, idManual = "PRE" ,pressureAvg = 100.0, pressureSystolic = 120.0, pressureDiastolic = 80.0, scanDate = "26/10/2018", pressureSystolicManual = 0.0, pressureDiastolicManual = 0.0, pressureAvgManual = 0.0))
        scanList.add(Scan(brazo = false, idManual = "JLP" ,pressureAvg = 106.0, pressureSystolic = 117.0, pressureDiastolic = 72.0, scanDate = "25/10/2018", pressureSystolicManual = 0.0, pressureDiastolicManual = 0.0, pressureAvgManual = 0.0))
        scanList.add(Scan(brazo = true, idManual = "" ,pressureAvg = 97.0, pressureSystolic = 119.0, pressureDiastolic = 75.0, scanDate = "24/10/2018", pressureSystolicManual = 0.0, pressureDiastolicManual = 0.0, pressureAvgManual = 0.0))
        scanList.add(Scan(brazo = false, idManual = "HLJ" ,pressureAvg = 102.0, pressureSystolic = 124.0, pressureDiastolic = 79.0, scanDate = "24/10/2018", pressureSystolicManual = 0.0, pressureDiastolicManual = 0.0, pressureAvgManual = 0.0))
        scanList.add(Scan(brazo = true, idManual = "EAO" ,pressureAvg = 104.0, pressureSystolic = 126.0, pressureDiastolic = 80.0, scanDate = "23/10/2018", pressureSystolicManual = 0.0, pressureDiastolicManual = 0.0, pressureAvgManual = 0.0))
    }
}


