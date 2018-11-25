package com.edgardo.corasensor.activities

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.edgardo.corasensor.R
import kotlinx.android.synthetic.main.activity_scan.*
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import com.edgardo.corasensor.database.ScanDatabase
import com.edgardo.corasensor.networkUtility.BluetoothConnectionService
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_settings.*
import java.lang.Exception
import java.util.*
import java.util.concurrent.TimeUnit


class ScanActivity : AppCompatActivity(), AdapterView.OnItemClickListener {

    val _tag = "ActivityScan"
    // List of bluetooth devices
    var btDevices = ArrayList<BluetoothDevice>()
    // Selected devices
    lateinit var selectedBtDevices: BluetoothDevice
    // Communication UUID
    private val uuidConnection = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    // List adapter
    // Bluetooth adapter
    var btAdapter: BluetoothAdapter? = null
    // Bluetooth connection
    lateinit var btConnection: BluetoothConnectionService
    lateinit var instanceDatabase: ScanDatabase
    lateinit var progressDialogConnection: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        btDevices = ArrayList()

        //Broadcasts when bond state changes (ie:pairing)
        val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        registerReceiver(pairingStatusChange, filter)

        btAdapter = BluetoothAdapter.getDefaultAdapter()

        validateBTOn()
        // Check for permissions on manifest
//        checkBTPermissions()

        discover()

        button_cancel.setOnClickListener { onClick(it) }
        button_finish.setOnClickListener { onClick(it) }
    }

    private fun onClick(v: View) {
        when (v.id) {
            R.id.button_cancel -> {
                finish()
            }
            R.id.button_finish -> {
                val intent = Intent(this, DetailActivity::class.java)
                startActivity(intent)
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
//        Toast
//                .makeText(this, applicationContext.getString(R.string.msg_bt_searching),
//                        Toast.LENGTH_SHORT)
//                .show()

        // Progress for connection
        progressDialogConnection = ProgressDialog.show(applicationContext, applicationContext.getString(R.string.msg_bt_searching), "Please Wait...", true)

        // Check for permissions on manifest
        checkBTPermissions()

        enableDicvoveringMode()

        if (btAdapter!!.isDiscovering) {
            btAdapter!!.cancelDiscovery()
            Log.d(_tag, "Discovering: Canceling discovery.")


            btAdapter!!.startDiscovery()
            val discoverDevicesIntent = IntentFilter(BluetoothDevice.ACTION_FOUND)
            registerReceiver(setDevices, discoverDevicesIntent)
        }
        if (!btAdapter!!.isDiscovering) {

            btAdapter!!.startDiscovery()
            val discoverDevicesIntent = IntentFilter(BluetoothDevice.ACTION_FOUND)

            registerReceiver(onBTChangeState, discoverDevicesIntent)
            registerReceiver(setDevices, discoverDevicesIntent)

        }
    }


    /**
     *  Receiver for a list of not paired devices
     * -Executed by startDiscover() method.
     *  Update the Adapter list
     */
    private val setDevices = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            Log.d(_tag, "onReceive: ACTION FOUND.")

            if (action == BluetoothDevice.ACTION_FOUND) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                if (device.name != null) {
                    btDevices.add(device)
                    if (btDevices.size >= 5) {
                        val discoverDevicesIntent = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)

                        registerReceiver(onBTChangeState, discoverDevicesIntent)
                    }
                    Log.d(_tag, "onReceive: " + device.name + ": " + device.address)
//                    val devicesBTListAdapter = DevicesBTListAdapter(context, R.layout.row_devices_bt, btDevices)
//                    list_new_devices.adapter = devicesBTListAdapter
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

//        Log.d(_tag, "onItemClick: deviceName = $deviceName")
//        Log.d(_tag, "onItemClick: deviceAddress = $deviceAddress")

        //create the bond.

        Log.d(_tag, "Trying to pair with $deviceName")
        Toast
                .makeText(this, applicationContext.getString(R.string.msg_bt_pairing) + deviceName,
                        Toast.LENGTH_SHORT)
                .show()
        btDevices[i].createBond()

        selectedBtDevices = btDevices[i]
        btConnection = BluetoothConnectionService(this)

        if (selectedBtDevices.address != null) {
            startBTConnection(selectedBtDevices, uuidConnection)
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
                    Manifest.permission.ACCESS_COARSE_LOCATION), SettingsActivity.BLUETOOTH_REQUEST_PERMISSION)

        } else {
//            Log.d(_tag, "Permission: DENIED")

        }

    }


    /**
     * starting listening service method
     */
    private fun startBTConnection(device: BluetoothDevice, uuid: UUID) {
        Log.d(_tag, "startBTConnection: Initializing RFCOM Bluetooth Connection.")

        btConnection.startClient(device, uuid)
                .debounce(300, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    try {
                        val parData = it.split(";")
                        // ms Time; mmHG; pulso
                        response_data.append("${parData[0]} - ${parData[1]} - ${parData[2]} \n")
//                        val currentTime = parData[0].toDouble()

//                        val scanData = ScanData(currentTime, parData[2].toDouble(), parData[1].toDouble(), 1)
//                        Executor.ioThread {
//                            Log.d(_tag, "Create")
//                            instanceDatabase.scanDataDao().insertScanData(scanData)
//                        }
                    } catch (e: Exception) {
                        response_data.append("Error ${it} \n")
                        Log.d(_tag, "Data is no in correct format")
                    }

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
                progressDialogConnection.dismiss()
                Toast.makeText(applicationContext, "Finish discovery",
                        Toast.LENGTH_SHORT).show()
                setAlertDialogList()

            } else if (action == BluetoothAdapter.ACTION_DISCOVERY_STARTED) {

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

    private fun setAlertDialogList() {


        val alert: AlertDialog.Builder = AlertDialog.Builder(this)

        alert.setTitle("Devices")

        val list = ListView(this)
        list.adapter = DevicesBTListAdapter(this, R.layout.row_devices_bt, btDevices)
        list.onItemClickListener = this

        alert.setView(list)
        alert.show()


    }

    override fun onDestroy() {
        Log.d(_tag, "onDestroy: called.")
        super.onDestroy()

        try {
            unregisterReceiver(pairingStatusChange)
            unregisterReceiver(setDevices)

            unregisterReceiver(onBTChangeState)
            unregisterReceiver(changeOnAction)
        } catch (e: Exception) {

        }

    }


}
