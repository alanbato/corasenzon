package com.edgardo.corasensor.database

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.edgardo.corasensor.Scan.Scan

@Dao
interface ScanDao {
    @Query("SELECT * FROM Scan ORDER BY _id")
    fun loadAllScan(): LiveData<List<Scan>>

    @Insert
    fun insertScanList(medicion: List<Scan>)

    @Query("SELECT COUNT (*) FROM Scan")
    fun getAnyScan(): Int

    @Insert
    fun insertScan(medicion: Scan)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateScan(medicion: Scan)

    @Delete
    fun deleteScan(medicion: Scan)

    @Query("SELECT * FROM Scan WHERE _id = :id")
    fun loadScanById(id: Int): Scan

    // TODO: Agregar query con join de la tabla de scan y dato para un scan
}
