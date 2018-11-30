// HearAssistent
//
//Copyright (C) 2018 - ITESM
//
//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program.  If not, see <http://www.gnu.org/licenses/>.

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
    fun insertScan(medicion: Scan): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateScan(medicion: Scan)

    @Delete
    fun deleteScan(medicion: Scan)

    @Query("SELECT * FROM Scan WHERE _id = :id")
    fun loadScanById(id: Long): Scan


    // TODO: Agregar query con join de la tabla de scan y dato para un scan
}
