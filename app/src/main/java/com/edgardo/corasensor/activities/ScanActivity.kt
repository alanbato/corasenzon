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
import com.edgardo.corasensor.helpers.calculate
import com.edgardo.corasensor.networkUtility.BluetoothConnectionService
import com.edgardo.corasensor.networkUtility.Executor.Companion.ioThread
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_scan.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

import android.graphics.Canvas
import android.graphics.Paint

import android.widget.Toast
import android.R.attr.data
import android.animation.ObjectAnimator
import android.text.TextPaint


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

    lateinit var time_measure: ArrayList<Double>
    lateinit var pressure: ArrayList<Double>
    lateinit var result: ArrayList<Double>
    lateinit var needle: Needle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        btDevices = ArrayList()
        btAdapter = BluetoothAdapter.getDefaultAdapter()
        btConnection = BluetoothConnectionService(this)

        instanceDatabase = ScanDatabase.getInstance(this)


        val layout1 = findViewById<android.support.constraint.ConstraintLayout>(R.id.manometro)
        val canvass = Canvass(this, 230f, false)
        val white = Canvass(this, 180f, true)
        needle = Needle(this)
        var rotation = 30f
        var nameVal = 20
        for (i in 0..10) {
            val grade = Grade(this, rotation, nameVal)
            layout1.addView(grade)
            rotation = rotation + 30f
            nameVal += 20
        }
        layout1.addView(needle)
        layout1.addView(canvass)
        layout1.addView(white)
        needle.setOnClickListener(needle)


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
            }
        }
        button_cancel.setOnClickListener {
            finish()
        }
        button_finish.setOnClickListener { onClick(it) }

    }


    inner class Grade(context: Context, var rotate: Float, var name: Int) : View(context) {
        val paint = Paint()
        val textPaint = TextPaint().apply {
            textSize = 60f
            rotation = -rotate
        }

        override fun onDraw(canvas: Canvas) {
            val width = getWidth()
            val centerX = width.toFloat() / 2
            val centerY = height.toFloat() / 2
            paint.strokeWidth = 15f
            canvas.drawLine(centerX, centerY, centerX, centerY + 320, paint).apply {
                rotation = rotate
            }
            canvas.drawText(name.toString(), centerX, centerY + 370, textPaint).apply {
                rotation = rotate
            }
        }
    }

    inner class Canvass(context: Context, var radius: Float, var white: Boolean)
        : View(context), View.OnClickListener {
        val paint = Paint()
        val textPaint = TextPaint().apply {
            textSize = 80f
        }
        var pressureVal: Int = 90
        override fun onDraw(canvas: Canvas) {
            val width = getWidth()
            val centerX = width.toFloat() / 2
            val centerY = height.toFloat() / 2
            if (white) {
                paint.setARGB(255, 250, 250, 255)
                canvas.drawCircle(centerX, centerY, this.radius, paint)
                canvas.drawText(pressureVal.toString(), centerX - 30, centerY + 30, textPaint)
            } else {
                paint.setARGB(255, 200, 200, 200)
                canvas.drawCircle(centerX, centerY, this.radius, paint)
            }
        }

        override fun onClick(v: View?) {
            pressureVal += 10
        }
    }

    inner class Needle(context: Context) : View(context), View.OnClickListener {
        val paint = Paint()
        val textPaint = TextPaint().apply { textSize = 16f }
        override fun onDraw(canvas: Canvas) {
            val width = getWidth()
            paint.setARGB(255, 255, 0, 0)
            val centerX = width.toFloat() / 2
            val centerY = height.toFloat() / 2
            val downY = centerY + 300f
            paint.strokeWidth = 20f
            canvas.drawLine(centerX, centerY, centerX, downY, paint).apply {
                rotation = 30f
            }
        }

        override fun onClick(v: View?) {
            ObjectAnimator.ofFloat(v, "rotation", v!!.rotation + 10f).start()
        }

    }

    private fun updateValue(v: View?, newVal: Float) {
        var new_rotation = newVal - v!!.rotation - 20
        if (new_rotation > 260f) {
            new_rotation = 280f
        } else if (new_rotation < 20f) {
            new_rotation = 20f
        }
        new_rotation += 50f

        ObjectAnimator.ofFloat(v, "rotation", new_rotation).start()
    }


    private fun addEntry(tiempo: Double, presion: Double) {
        if (firstTime != 0.0) {
            firstTime = tiempo
        }
        var newTime = tiempo - firstTime

        series.appendData(DataPoint(newTime, presion), true, 300)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        finish()
    }

    private fun onClick(v: View) {
        when (v.id) {
            R.id.button_cancel -> {
                finish()
            }
            R.id.button_finish -> {

                result = calculate(this, pressure, time_measure)
                //El scan que se crea con los datos
                val scan = Scan(brazo = true, idManual = "Prueba", pressureAvg = 100.0, pressureSystolic = result[1], pressureDiastolic = result[0], scanDate = "26/10/2018", pressureSystolicManual = 0.0, pressureDiastolicManual = 0.0, pressureAvgManual = 0.0)
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
                .debounce(5, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    try {
                        val processData = it.split(";")
                        updateValue(needle, processData[1].toFloat())
                        addEntry(processData[0].toDouble(), processData[1].toDouble())
                        Log.d(_tag, "Value " + it)
                        time_measure.add(processData[0].toDouble())
                        pressure.add(processData[1].toDouble())

                        // ms Time; mmHG; pulso
                        //response_data.append("${parData[0]} - ${parData[1]} - ${parData[2]} \n")
                    } catch (e: Exception) {
                        Log.d(_tag, "Data is no in correct format" + it)
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