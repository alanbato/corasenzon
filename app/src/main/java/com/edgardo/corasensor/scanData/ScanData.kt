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

package com.edgardo.corasensor.scanData

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.TypeConverters
import android.os.Parcel
import android.os.Parcelable
import com.edgardo.corasensor.helpers.Converters

@Entity(tableName = "ScanData")
@TypeConverters(Converters::class)

data class ScanData(@ColumnInfo(name = "Time") var time: Double?,
                    @ColumnInfo(name = "Pulse") var pulse: Double?,
                    @ColumnInfo(name = "Pressure") var pressure: Double?,
                    @ColumnInfo(name = "fk_scan") var fk_sacan: Int?
) : Parcelable {
    @ColumnInfo(name = "_id")
    @PrimaryKey(autoGenerate = true)
    var _id: Int = 0

    constructor(parcel: Parcel) : this(
            parcel.readValue(Double::class.java.classLoader) as? Double,
            parcel.readValue(Double::class.java.classLoader) as? Double,
            parcel.readValue(Double::class.java.classLoader) as? Double,
            parcel.readValue(Int::class.java.classLoader) as? Int) {
        _id = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(time)
        parcel.writeValue(pulse)
        parcel.writeValue(pressure)
        parcel.writeValue(fk_sacan)
        parcel.writeInt(_id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ScanData> {
        override fun createFromParcel(parcel: Parcel): ScanData {
            return ScanData(parcel)
        }

        override fun newArray(size: Int): Array<ScanData?> {
            return arrayOfNulls(size)
        }
    }

}