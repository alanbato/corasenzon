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

package com.edgardo.corasensor.activities

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.*
import com.edgardo.corasensor.HeartAssistantApplication
import com.edgardo.corasensor.R
import com.edgardo.corasensor.database.ScanDatabase
import com.edgardo.corasensor.networkUtility.BluetoothConnectionService
import com.edgardo.corasensor.networkUtility.Executor.Companion.ioThread
import com.edgardo.corasensor.scanData.ScanData
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_settings.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class SettingsActivity : AppCompatActivity(), AdapterView.OnItemClickListener {

    val _tag = "SettingAct"
    // List of bluetooth devices
    var btDevices = ArrayList<BluetoothDevice>()
    // Selected devices
    lateinit var selectedBtDevices: BluetoothDevice
    // Communication UUID
    private val uuidConnection = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    // Bluetooth adapter
    var btAdapter: BluetoothAdapter? = null

    // Bluetooth connection
    lateinit var btConnection: BluetoothConnectionService
    lateinit var instanceDatabase: ScanDatabase

    val formatter = SimpleDateFormat("HH:mm:ss.SSSXXX")


    companion object {
        const val BLUETOOTH_REQUEST_PERMISSION = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        instanceDatabase = ScanDatabase.getInstance(this)


        supportActionBar!!.title = applicationContext.getString(R.string.action_settings)

        btDevices = ArrayList()

        //Broadcasts when bond state changes (ie:pairing)
        val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        registerReceiver(pairingStatusChange, filter)

        btAdapter = BluetoothAdapter.getDefaultAdapter()
        validateBTOn()

        list_new_devices.onItemClickListener = this
        button_discover.setOnClickListener { click(it) }

        val application = application
        if (application is HeartAssistantApplication) {
            val device = application.device
            if (device != null) {
                default_devices.text = "Device set ${device.name}"
            } else {
                default_devices.text = "No divice set"
            }
        }

    }

    /**
     * Function to validate if the phone have BT and check if is turn on
     */
    private fun validateBTOn() {
        if (btAdapter == null) {
            AlertDialog.Builder(this).setMessage(
                    applicationContext.getString(R.string.device_bt_capability)
            ).setCancelable(false)
        }
        // if bluetooth is off
        if (!btAdapter!!.isEnabled) {
            val alert = AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage(applicationContext.getString(R.string.msg_enable_bluetooth))
                    .setCancelable(true)
                    .setPositiveButton("Ok") { dialog, which ->
                        // Enable BT
                        val enableBT = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        startActivity(enableBT)
                        // Notify changes on BT status
                        val btIntent = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
                        registerReceiver(changeOnAction, btIntent)
                    }
                    .setNegativeButton("Cancel") { dialog, which ->
                        // Back to Home
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    }

            alert.show()

        }

    }

    /**
     * Button click action
     */
    private fun click(v: View) {

        when (v.id) {
            R.id.button_discover -> {
                btDevices = ArrayList()
                discover()
            }
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
     * Start looking fot devices
     */
    private fun discover() {
        Log.d(_tag, "Discovering: Looking for unpaired devices.")
        Toast
                .makeText(this, applicationContext.getString(R.string.msg_bt_searching),
                        Toast.LENGTH_SHORT)
                .show()

        // Check for permissions on manifest
        checkBTPermissions()
        //enableDicvoveringMode()

        if (btAdapter!!.isDiscovering) {
            btAdapter!!.cancelDiscovery()
            Log.d(_tag, "Discovering: Canceling discovery.")


            btAdapter!!.startDiscovery()
            val discoverDevicesIntent = IntentFilter(BluetoothDevice.ACTION_FOUND)
            registerReceiver(updateListAdapter, discoverDevicesIntent)
        }
        if (!btAdapter!!.isDiscovering) {

            btAdapter!!.startDiscovery()
            val discoverDevicesIntent = IntentFilter(BluetoothDevice.ACTION_FOUND)

            registerReceiver(onBTChangeState, discoverDevicesIntent)
            registerReceiver(updateListAdapter, discoverDevicesIntent)

        }
    }


    /**
     *  Receiver for a list of not paired devices
     * -Executed by startDiscover() method.
     *  Update the Adapter list
     */
    private val updateListAdapter = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            Log.d(_tag, "onReceive: ACTION FOUND.")

            if (action == BluetoothDevice.ACTION_FOUND) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                if (device.name != null) {
                    btDevices.add(device)
                    Log.d(_tag, "onReceive: " + device.name + ": " + device.address)
                    val devicesBTListAdapter = DevicesBTListAdapter(context, R.layout.row_devices_bt, btDevices)
                    list_new_devices.adapter = devicesBTListAdapter
                }

            }
        }
    }


    override fun onItemClick(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
        //first cancel discovery because its very memory intensive.
        btAdapter!!.cancelDiscovery()

        Log.d(_tag, "onItemClick: You Clicked on a device.")
        val deviceName = btDevices[i].name
//        val deviceAddress = btDevices[i].address

        //create the bond.

        Log.d(_tag, "Trying to pair with $deviceName")
        Toast
                .makeText(this, applicationContext.getString(R.string.msg_bt_pairing) + deviceName,
                        Toast.LENGTH_SHORT)
                .show()
        //btDevices[i].createBond()

        selectedBtDevices = btDevices[i]

        if (selectedBtDevices.address != null) {
            val application = application
            if (application is HeartAssistantApplication) {
                application.device = btDevices[i]
                application.uuidConnection = uuidConnection

                Toast.makeText(this, "Device Saved", Toast.LENGTH_SHORT).show()
                finish()
            }

            //startBTConnection(selectedBtDevices, uuidConnection)
        }else{
            Toast.makeText(this, "This device can't be saved", Toast.LENGTH_SHORT).show()
        }

    }


    /**
     * Request permission to access bluetooth
     */
    private fun checkBTPermissions() {

        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED)) {
            Log.d(_tag, "Permission: GRANTED")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION), BLUETOOTH_REQUEST_PERMISSION)

        } else {
            Log.d(_tag, "Permission: DENIED")

        }

    }



    /**
     * Make device visible for 300 seconds
     */
    private fun enableDicvoveringMode() {
        Log.d(_tag, "enableDicvoveringMode: Making device discoverable for 300 seconds.")

        Toast
                .makeText(this, applicationContext.getString(R.string.msg_bt_discovering_mode),
                        Toast.LENGTH_SHORT)
                .show()

        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        startActivity(discoverableIntent)

        val intentFilter = IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
        registerReceiver(onBTChangeState, intentFilter)
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
                Toast.makeText(applicationContext, "Finish discovery",
                        Toast.LENGTH_SHORT).show()

            }
        }
    }


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
                    Toast.makeText(applicationContext, "Connecting to devices",
                            Toast.LENGTH_SHORT).show()

                }
                // Connection lost
                if (device.bondState == BluetoothDevice.BOND_NONE) {
                    Log.d(_tag, "BroadcastReceiver: BOND_NONE.")
                    Toast.makeText(applicationContext, applicationContext.getString(R.string.msg_bt_connection_lost),
                            Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    override fun onDestroy() {
        Log.d(_tag, "onDestroy: called.")
        super.onDestroy()
        try {
            unregisterReceiver(onBTChangeState)
            unregisterReceiver(changeOnAction)
            unregisterReceiver(pairingStatusChange)
            unregisterReceiver(updateListAdapter)
        } catch (e: Exception) {
            Log.d("ErrorBT: ", e.toString())
        }

    }

}
