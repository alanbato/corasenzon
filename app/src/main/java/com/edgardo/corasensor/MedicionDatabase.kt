package com.edgardo.corasensor

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context


@Database(entities = arrayOf(Medicion::class), version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class MedicionDatabase : RoomDatabase(){
    abstract fun medicionDao(): MedicionDao

    companion object {
        private val DATABASE_NAME = "MedicionDB.db"
        private var dbInstance: MedicionDatabase? = null


        @Synchronized
        fun getInstance(context: Context): MedicionDatabase
        {
            if (dbInstance == null) {
                dbInstance = buildDatabase(context)
            }
            return dbInstance!!
        }

        private fun buildDatabase(context: Context): MedicionDatabase {
            return Room.databaseBuilder(context, MedicionDatabase::class.java, DATABASE_NAME).fallbackToDestructiveMigration().build()
        }
    }
}