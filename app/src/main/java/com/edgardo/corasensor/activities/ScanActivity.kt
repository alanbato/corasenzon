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
import com.edgardo.corasensor.HeartAssistantApplication
import com.edgardo.corasensor.database.ScanDatabase
import com.edgardo.corasensor.networkUtility.BluetoothConnectionService
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_settings.*
import java.lang.Exception
import java.util.*
import java.util.concurrent.TimeUnit


class ScanActivity : AppCompatActivity() {

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
        btAdapter = BluetoothAdapter.getDefaultAdapter()
        btConnection = BluetoothConnectionService(this)



        validateBTOn()
        val application = application
        if (application is HeartAssistantApplication) {
            val device = application.device
            val uuid = application.uuidConnection
            if (device != null && uuid != null) {
                startBTConnection(device,uuid)

            } else {
                // Back to Home
                finish()
//                val intent = Intent(this, MainActivity::class.java)
//                startActivity(intent)
            }
        }

        // Check for permissions on manifest
//        checkBTPermissions()

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
     * starting listening service method
     */
    private fun startBTConnection(device: BluetoothDevice, uuid: UUID) {
        Log.d(_tag, "startBTConnection: Initializing RFCOM Bluetooth Connection.")

        btConnection.startClient(device, uuid)
                .debounce(10, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    try {
                        //val parData = it.split(";")
                        Log.d(_tag, it)
                        // ms Time; mmHG; pulso
                        //response_data.append("${parData[0]} - ${parData[1]} - ${parData[2]} \n")
//                        val currentTime = parData[0].toDouble()

//                        val scanData = ScanData(currentTime, parData[2].toDouble(), parData[1].toDouble(), 1)
//                        Executor.ioThread {
//                            Log.d(_tag, "Create")
//                            instanceDatabase.scanDataDao().insertScanData(scanData)
//                        }
                    } catch (e: Exception) {
                        //response_data.append("Error ${it} \n")
                        Log.d(_tag, "Data is no in correct format")
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


    override fun onDestroy() {
        Log.d(_tag, "onDestroy: called.")
        super.onDestroy()
        try {
            unregisterReceiver(changeOnAction)
        } catch (e: Exception) {

        }

    }


}
