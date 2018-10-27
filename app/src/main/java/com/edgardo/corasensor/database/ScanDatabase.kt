package com.edgardo.corasensor.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context
import com.edgardo.corasensor.helpers.Converters
import com.edgardo.corasensor.scanData.ScanData
import com.edgardo.corasensor.Scan.Scan


@Database(entities = [Scan::class, ScanData::class], version = 2, exportSchema = false)
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
