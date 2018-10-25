package com.edgardo.corasensor

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.TypeConverters
import android.os.Parcel
import android.os.Parcelable
import java.util.*

@Entity(tableName = "Dato")
@TypeConverters(Converters::class)
data class Dato (@ColumnInfo(name = "tiempo") var tiempo: Double?,
                 @ColumnInfo(name = "pulso") var pulso: Double?,
                 @ColumnInfo(name = "presion") var presion: Double?)
    : Parcelable {
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
        parcel.writeValue(tiempo)
        parcel.writeValue(pulso)
        parcel.writeValue(presion)
        parcel.writeInt(_id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Dato> {
        override fun createFromParcel(parcel: Parcel): Dato {
            return Dato(parcel)
        }

        override fun newArray(size: Int): Array<Dato?> {
            return arrayOfNulls(size)
        }
    }
}