package com.edgardo.corasensor

import android.arch.persistence.room.TypeConverters
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import java.io.ByteArrayOutputStream
import android.content.Context
import android.graphics.BitmapFactory
import android.provider.MediaStore.Images.Media.getBitmap

@TypeConverters(Converters::class)
class medicionDummy (var context: Context ) {
    val listaMediciones: MutableList<Medicion> = ArrayList()


    private fun getBitmap(imageId:Int): Bitmap = BitmapFactory.decodeResource(context.resources,imageId)

    init {
        dataList()
    }

    fun dataList(){
        listaMediciones.add(Medicion(123.0,455.0,434.0))
        listaMediciones.add(Medicion(345.0,367.0,886.0))
        listaMediciones.add(Medicion(176.0,134.0,654.0))

    }
}


