package com.edgardo.corasensor.database

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.edgardo.corasensor.scanData.ScanData

@Dao
interface ScanDataDao{
    @Query("SELECT * FROM ScanData ORDER BY time")
    fun loadAllScanDatav(): LiveData<List<ScanData>>

    @Insert
    fun insertDatoList(data:List<ScanData>)

    @Query("SELECT COUNT (*) FROM ScanData")
    fun getAnyScanData(): Int

    @Insert
    fun insertScanData(data: ScanData)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateScanData(data: ScanData)

    @Delete
    fun deleteScanData(data: ScanData)

    @Query("SELECT * FROM ScanData WHERE _id = :id")
    fun loadScanDataById(id: Int) : ScanData

    @Query("Select * FROM ScanData WHERE fk_scan = :id")
    fun loadScanDataByScanId(id:Int): List<ScanData>
}
