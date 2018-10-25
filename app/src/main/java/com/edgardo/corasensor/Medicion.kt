package com.edgardo.corasensor

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.TypeConverters
import android.os.Parcel
import android.os.Parcelable

@Entity(tableName = "Medicion")
@TypeConverters(Converters::class)
data class Medicion (@ColumnInfo(name = "presionMedia") var presionMedia: Double?,
                  @ColumnInfo(name = "presionSiastolica") var presionSiastolica: Double?,
                  @ColumnInfo(name = "presionDiastolica") var presionDiastolica: Double?) : Parcelable {
    @ColumnInfo(name = "_id")
    @PrimaryKey(autoGenerate = true)
    var _id:Int = 0

    constructor(parcel: Parcel) : this(
            parcel.readValue(Double::class.java.classLoader) as? Double,
            parcel.readValue(Double::class.java.classLoader) as? Double,
            parcel.readValue(Double::class.java.classLoader) as? Double) {
        _id = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(presionMedia)
        parcel.writeValue(presionSiastolica)
        parcel.writeValue(presionDiastolica)
        parcel.writeInt(_id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Medicion> {
        override fun createFromParcel(parcel: Parcel): Medicion {
            return Medicion(parcel)
        }

        override fun newArray(size: Int): Array<Medicion?> {
            return arrayOfNulls(size)
        }
    }

}