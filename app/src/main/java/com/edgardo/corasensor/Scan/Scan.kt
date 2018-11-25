package com.edgardo.corasensor.Scan

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.TypeConverters
import android.os.Parcel
import android.os.Parcelable
import com.edgardo.corasensor.helpers.Converters

@Entity(tableName = "Scan")
@TypeConverters(Converters::class)
data class Scan(@ColumnInfo(name = "PressureScanAvg") var pressureAvg: Double?,
                @ColumnInfo(name = "PressureSystolic") var pressureSystolic: Double?,
                @ColumnInfo(name = "PressureDiastolic") var pressureDiastolic: Double?,
                @ColumnInfo(name = "PressureScanManual") var pressureAvgManual: Double? = null,
                @ColumnInfo(name = "PressureSystolicManual") var pressureSystolicManual: Double? = null,
                @ColumnInfo(name = "PressureDiastolicManual") var pressureDiastolicManual: Double? = null,
                @ColumnInfo(name = "ScanDate") var scanDate: String?,
                @ColumnInfo(name = "idManual") var idManual: String?

) : Parcelable {
    @ColumnInfo(name = "_id")
    @PrimaryKey(autoGenerate = true)
    var _id: Int = 0

    constructor(parcel: Parcel) : this(
            parcel.readValue(Double::class.java.classLoader) as? Double,
            parcel.readValue(Double::class.java.classLoader) as? Double,
            parcel.readValue(Double::class.java.classLoader) as? Double,
            parcel.readValue(Double::class.java.classLoader) as? Double,
            parcel.readValue(Double::class.java.classLoader) as? Double,
            parcel.readValue(Double::class.java.classLoader) as? Double,
            parcel.readString(),
            parcel.readString()) {
        _id = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(pressureAvg)
        parcel.writeValue(pressureSystolic)
        parcel.writeValue(pressureDiastolic)
        parcel.writeValue(pressureAvgManual)
        parcel.writeValue(pressureSystolicManual)
        parcel.writeValue(pressureDiastolicManual)
        parcel.writeString(scanDate)
        parcel.writeString(idManual)
        parcel.writeInt(_id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Scan> {
        override fun createFromParcel(parcel: Parcel): Scan {
            return Scan(parcel)
        }

        override fun newArray(size: Int): Array<Scan?> {
            return arrayOfNulls(size)
        }
    }

}