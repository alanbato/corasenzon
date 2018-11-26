package com.edgardo.corasensor

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_settings.*
import java.util.*

class HeartAssistantApplication : Application() {

    private val _tag = "ApplicationHeart"

    var device: BluetoothDevice? = null
    var uuidConnection : UUID? = null

}