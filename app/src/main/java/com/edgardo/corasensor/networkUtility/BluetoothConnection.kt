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

package com.edgardo.corasensor.networkUtility

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
import com.edgardo.corasensor.R
import com.edgardo.corasensor.activities.MainActivity
import java.util.*


class BluetoothConnection(internal var context: Context) {


    val _tag = "BT_Connection"

    // Bluetooth adapter
    var btAdapter: BluetoothAdapter? = null

    // List of bluetooth devices
    var btDevices = ArrayList<BluetoothDevice>()
    var btDevicesName = ArrayList<String>()


    // Selected devices
    lateinit var selectedBtDevices: BluetoothDevice

    // BT code permission
    val BLUETOOTH_REQUEST_PERMISSION = 1001


    /**
     * Broadcast Receiver that detects bond state changes (Pairing status changes)
     * Check for status pairing
     */
    private val pairingStatusChange = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            if (action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {

                // Get action
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

                // Connection already exist
                if (device.bondState == BluetoothDevice.BOND_BONDED) {
                    Log.d(_tag, "BroadcastReceiver: BOND_BONDED.")
                    // Set actual devices
                    selectedBtDevices = device
                }
                // Creating new connection
                if (device.bondState == BluetoothDevice.BOND_BONDING) {
                    Log.d(_tag, "BroadcastReceiver: BOND_BONDING.")
                    Toast.makeText(context, "Connecting to devices",
                            Toast.LENGTH_SHORT).show()

                }
                // Connection lost
                if (device.bondState == BluetoothDevice.BOND_NONE) {
                    Log.d(_tag, "BroadcastReceiver: BOND_NONE.")
//                    Toast.makeText(context, context.getString(R.string.msg_bt_connection_lost),
//                            Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    init {
        btDevices = ArrayList()
        btAdapter = BluetoothAdapter.getDefaultAdapter()
        //Broadcasts when bond state changes (ie:pairing)
        val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        context.registerReceiver(pairingStatusChange, filter)
    }



    /**
     * Function to validate if the phone have BT and check if is turn on
     */
    fun validateBTOn() {
        if (btAdapter == null) {
            AlertDialog.Builder(context).setMessage(
                    context.getString(R.string.device_bt_capability)
            ).setCancelable(false)
        }
        // if bluetooth is off
        if (!btAdapter!!.isEnabled) {
            val alert = AlertDialog.Builder(context)
                    .setTitle("Error")
                    .setMessage(context.getString(R.string.msg_enable_bluetooth))
                    .setCancelable(true)
                    .setPositiveButton("Ok") { dialog, which ->
                        // Enable BT
                        val enableBT = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        context.startActivity(enableBT)
                        // Notify changes on BT status
                        val btIntent = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
                        context.registerReceiver(changeOnAction, btIntent)
                    }
                    .setNegativeButton("Cancel") { dialog, which ->
                        // Back to Home
                        val intent = Intent(context, MainActivity::class.java)
                        context.startActivity(intent)
                    }

            alert.show()

        }

    }


    /**
     * Create a BroadcastReceiver for ACTION_FOUND
     * Verify if BT status has changes
     */
    private val changeOnAction = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            // When discovery finds a device
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)

                when (state) {
                    BluetoothAdapter.STATE_OFF -> Log.d(_tag, "onReceive: STATE OFF")
                    BluetoothAdapter.STATE_TURNING_OFF -> Log.d(_tag, "changeOnAction: STATE TURNING OFF")
                    BluetoothAdapter.STATE_ON -> Log.d(_tag, "changeOnAction: STATE ON")
                    BluetoothAdapter.STATE_TURNING_ON -> Log.d(_tag, "changeOnAction: STATE TURNING ON")
                }
            }
        }
    }

    /**
     * Request permission to access bluetooth
     */
    fun checkBTPermissions() {

        if ((ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED)) {
            Log.d(_tag, "Permission: GRANTED")
            ActivityCompat.requestPermissions(Activity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION), BLUETOOTH_REQUEST_PERMISSION)

        } else {
//            Log.d(_tag, "Permission: DENIED")
        }

    }

    /**
     * Verify changes on bluetooth states such as: Discovering mode On/Off
     * Used by enableDicvoveringMode()
     */
    private val onBTChangeState = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            if (action == BluetoothAdapter.ACTION_SCAN_MODE_CHANGED) {

                val mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR)

                when (mode) {
                    //Device is in Discoverable Mode
                    BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE -> Log.d(
                            _tag,
                            "onBTChangeState: Discoverability Enabled."
                    )
                    //Device not in discoverable mode
                    BluetoothAdapter.SCAN_MODE_CONNECTABLE -> Log.d(
                            _tag,
                            "onBTChangeState: Discoverability Disabled. Able to receive connections."
                    )
                    BluetoothAdapter.SCAN_MODE_NONE -> Log.d(
                            _tag,
                            "onBTChangeState: Discoverability Disabled. Not able to receive connections."
                    )
                    BluetoothAdapter.STATE_CONNECTING -> Log.d(_tag, "onBTChangeState: Connecting....")
                    BluetoothAdapter.STATE_CONNECTED -> Log.d(_tag, "onBTChangeState: Connected.")
                }

            } else if (action == BluetoothAdapter.ACTION_DISCOVERY_FINISHED) {
                Toast.makeText(context, "Finish discovery",
                        Toast.LENGTH_SHORT).show()

            }
        }
    }


    /**
     *  Receiver for a list of not paired devices
     * -Executed by startDiscover() method.
     *  Update the device list
     */

    private val setDevices = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothDevice.ACTION_FOUND) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                if (device.name != null) {
                    btDevices.add(device)
                    btDevicesName.add(device.name)
                    Log.d(_tag, "onReceive: " + device.name + ": " + device.address)
                }
            }
        }

    }


    private fun setAlertDialogList() {

        //CharSequence[] cs = list.toArray(new CharSequence[list.size()]);
        val names = arrayOf<CharSequence>()
        for ((i, d) in btDevices.withIndex()) {
            names[i] = d.name
        }


        val alert: AlertDialog.Builder = AlertDialog.Builder(context)

        alert.setTitle("Devices")


        alert.setItems(names, { _, i ->
            selectedBtDevices = btDevices[i]
        })
        alert.create()
        alert.show()


    }


    fun onDestroy() {
        Log.d(_tag, "onDestroy: called.")
        context.unregisterReceiver(changeOnAction)
        context.unregisterReceiver(pairingStatusChange)
        context.unregisterReceiver(setDevices)
        context.unregisterReceiver(onBTChangeState)
    }


}