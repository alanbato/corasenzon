package com.edgardo.corasensor

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*

@Dao
interface DatoDao{
    @Query("SELECT * FROM Dato ORDER BY tiempo")
    fun loadAllDatos(): LiveData<List<Dato>>

    @Insert
    fun insertDatoList(dato:List<Dato>)

    @Query("SELECT COUNT (*) FROM Dato")
    fun getAnyDato(): Int

    @Insert
    fun insertDato(dato:Dato)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateDato(dato: Dato)

    @Delete
    fun deleteDato(dato: Dato)

    @Query("SELECT * FROM Dato WHERE _id = :id")
    fun loadDatoById(id: Int) : Dato
}
