package com.edgardo.corasensor.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.edgardo.corasensor.HeartAssistantApplication
import com.edgardo.corasensor.R
import com.edgardo.corasensor.Scan.Scan
import com.edgardo.corasensor.database.ScanDatabase
import com.edgardo.corasensor.networkUtility.BluetoothConnectionService
import com.edgardo.corasensor.networkUtility.Executor.Companion.ioThread
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_scan.*
import java.util.*
import java.util.concurrent.TimeUnit


class ScanActivity : AppCompatActivity() {
    lateinit var instanceDatabase: ScanDatabase

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

    lateinit var progressDialogConnection: ProgressDialog
    var firstTime = 0.0

    lateinit var series: LineGraphSeries<DataPoint>
    private var lastX: Double = 6.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        btDevices = ArrayList()
        btAdapter = BluetoothAdapter.getDefaultAdapter()
        btConnection = BluetoothConnectionService(this)

        instanceDatabase = ScanDatabase.getInstance(this)

        val graph = findViewById<View>(R.id.graph) as GraphView
        series = LineGraphSeries()
        graph.addSeries(series)
        val viewport = graph.viewport
        viewport.isYAxisBoundsManual = true
        viewport.setMinY(0.0)
        viewport.setMaxY(180.0)
        viewport.isScrollable = true
        viewport.isScalable = true



        validateBTOn()
        val application = application
        if (application is HeartAssistantApplication) {
            val device = application.device
            val uuid = application.uuidConnection
            if (device != null && uuid != null) {
                startBTConnection(device, uuid)

            } else {
                // Back to Home
                finish()
//                val intent = Intent(this, MainActivity::class.java)
//                startActivity(intent)
            }
        }
        button_cancel.setOnClickListener {
            finish()
        }
        button_finish.setOnClickListener { onClick(it) }

        // Check for permissions on manifest
//        checkBTPermissions()

    }

    private fun addEntry(tiempo: Double, presion: Double) {
        if (firstTime != 0.0){
            firstTime = tiempo
        }
        var newTime = tiempo - firstTime

        series.appendData(DataPoint(newTime, presion), true, 300)
    }


//    override fun onResume() {
//        super.onResume()
//        Thread(Runnable {
//            while (true) {
//                runOnUiThread { addEntry() }
//                try {
//                    Thread.sleep(20)
//                } catch (e: InterruptedException) {
//                    // manage error ...
//                }
//            }
//        }).start()
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        finish()
    }

    private fun onClick(v: View) {
        when (v.id) {
            R.id.button_cancel -> {
                finish()
            }
            R.id.button_finish -> {
                //El scan que se crea con los datos
                val scan = Scan(brazo = true, idManual = "Prueba", pressureAvg = 100.0, pressureSystolic = 120.0, pressureDiastolic = 80.0, scanDate = "26/10/2018", pressureSystolicManual = 0.0, pressureDiastolicManual = 0.0, pressureAvgManual = 0.0)
                ioThread {
                    instanceDatabase.scanDao().insertScan(scan)
                }

                val intent = Intent(this, DetailActivity::class.java)
                intent.putExtra("SCAN_KEY", scan)
                startActivityForResult(intent, 1)
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
                        val processData = it.split(";")
                        Log.d(_tag, it)

                        addEntry(processData[0].toDouble(), processData[1].toDouble())
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