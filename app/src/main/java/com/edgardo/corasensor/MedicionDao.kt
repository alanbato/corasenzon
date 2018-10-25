package com.edgardo.corasensor

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*

@Dao
interface MedicionDao{
    @Query("SELECT * FROM Medicion ORDER BY _id")
    fun loadAllMedicion(): LiveData<List<Medicion>>

    @Insert
    fun insertMedicionList(medicion:List<Medicion>)

    @Query("SELECT COUNT (*) FROM Medicion")
    fun getAnyMedicion(): Int

    @Insert
    fun insertMedicion(medicion:Medicion)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateMedicion(medicion: Medicion)

    @Delete
    fun deleteMedicion(medicion: Medicion)

    @Query("SELECT * FROM Dato WHERE _id = :id")
    fun loadMedicionById(id: Int) : Medicion
}
