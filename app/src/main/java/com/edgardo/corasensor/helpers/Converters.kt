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

package com.edgardo.corasensor.helpers

import android.arch.persistence.room.TypeConverter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class Converters {

    companion object {
        const val DATE_FORMAT: String = "yyyy/MM/dd"

        @TypeConverter
        @JvmStatic
        fun toString(date: Date?): String? {
            val format = SimpleDateFormat(DATE_FORMAT)
            return format.format(date)
        }

        @JvmStatic
        @TypeConverter
        fun toDate(dateString: String): Date? {
            return if (dateString == null) null else SimpleDateFormat(DATE_FORMAT).parse(dateString)
        }

        @TypeConverter
        fun toByteArray(bitmap: Bitmap): ByteArray {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream)
            return outputStream.toByteArray()
        }

        @TypeConverter
        fun toBitmap(image: ByteArray?): Bitmap {
            return BitmapFactory.decodeByteArray(image, 0, image!!.size)
        }


    }
}