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

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context
import com.edgardo.corasensor.helpers.Converters
import com.edgardo.corasensor.scanData.ScanData
import com.edgardo.corasensor.Scan.Scan


@Database(entities = [Scan::class, ScanData::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ScanDatabase : RoomDatabase() {
    abstract fun scanDao(): ScanDao
    abstract fun scanDataDao(): ScanDataDao

    companion object {
        private val DATABASE_NAME = "ScanPressureDB.db"
        private var dbInstance: ScanDatabase? = null


        @Synchronized
        fun getInstance(context: Context): ScanDatabase {
            if (dbInstance == null) {
                dbInstance = buildDatabase(context)
            }
            return dbInstance!!
        }

        private fun buildDatabase(context: Context): ScanDatabase {
            return Room.databaseBuilder(context, ScanDatabase::class.java, DATABASE_NAME).fallbackToDestructiveMigration().build()
        }
    }
}
