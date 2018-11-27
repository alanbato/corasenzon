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
