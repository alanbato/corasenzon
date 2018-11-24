package com.edgardo.corasensor

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.IntentFilter
import android.util.Log
import android.widget.Toast
import com.edgardo.corasensor.networkUtility.BluetoothConnection
import com.edgardo.corasensor.networkUtility.BluetoothConnectionService
import io.reactivex.Observable
import java.util.*

class HeartAssistantApplication : Application() {
    private val _tag = "ApplicationHeart"

    var scan: Observable<String>? = null
    var deviceName: String? = null


    override fun onCreate() {
        super.onCreate()

        val prefs = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        val btConnection = BluetoothConnection(this)
        btConnection.checkBTPermissions()
        btConnection.validateBTOn()

        btConnection.discover()
        Log.d(_tag, prefs.getString(BT_DEV_UUID, ""))
        val device = btConnection.findDevice(prefs.getString(BT_DEV_KEY, ""))
        if (device != null) {
            try{
                val uuid = UUID.fromString(prefs.getString(BT_DEV_UUID, ""))
                deviceName = device.name

                startBTConnection(device, uuid)
            }catch (e: Exception){
                Log.d(_tag, e.toString())
            }

        } else {
            Log.d(_tag, "No devices set ")
        }


    }

    fun startBTConnection(device: BluetoothDevice, uuid: UUID) {
        val btConnectionService = BluetoothConnectionService(this)
        try {
            Log.d(_tag, "${device.address} + ${device.name} ${uuid} ")
            scan = btConnectionService.startClient(device, uuid)
        } catch (e: Exception) {
            Toast.makeText(this, "Error connecting devices",
                    Toast.LENGTH_SHORT).show()
            Log.d(_tag, "Error connecting")
        }
    }


    companion object {
        const val PREFERENCES = "HEART_ASSISTANT"
        const val BT_DEV_KEY = "BT_DEVICE"
        const val BT_DEV_UUID = "BT_UUID"
    }
}