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

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextPaint
import android.util.Log
import android.view.View
import com.edgardo.corasensor.HeartAssistantApplication
import com.edgardo.corasensor.R
import com.edgardo.corasensor.Scan.Scan
import com.edgardo.corasensor.database.ScanDatabase
import com.edgardo.corasensor.helpers.Converters
import com.edgardo.corasensor.helpers.calculate
import com.edgardo.corasensor.networkUtility.BluetoothConnectionService
import com.edgardo.corasensor.networkUtility.Executor.Companion.ioThread
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.Viewport
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_scan.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class ScanActivity : AppCompatActivity() {
    lateinit var instanceDatabase: ScanDatabase
    var pressureVal: Int = 0
    val _tag = "ActivityScan"
    // List of bluetooth devices
    var btDevices = ArrayList<BluetoothDevice>()
    // Selected devices
    lateinit var selectedBtDevices: BluetoothDevice

    // Bluetooth adapter
    var btAdapter: BluetoothAdapter? = null
    // Bluetooth connection
    lateinit var btConnection: BluetoothConnectionService

    lateinit var progressDialogConnection: ProgressDialog
    var firstTime = 0.0

    lateinit var series: LineGraphSeries<DataPoint>
    lateinit var viewport: Viewport

    var time = 0
    var end_scan = false
    var lastPress = 0.0
    lateinit var time_measure: ArrayList<Long>
    lateinit var pressure: ArrayList<Double>
    lateinit var result: ArrayList<Double>
    lateinit var needle: Needle
    var runtime: Long = 0

    val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
    lateinit var canvass: Canvass
    lateinit var white: Canvass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        btDevices = ArrayList()
        time_measure = ArrayList()
        pressure = ArrayList()
        result = ArrayList()
        runtime = System.currentTimeMillis()

        btAdapter = BluetoothAdapter.getDefaultAdapter()
        btConnection = BluetoothConnectionService(this)

        instanceDatabase = ScanDatabase.getInstance(this)

        val layout1 = findViewById<android.support.constraint.ConstraintLayout>(R.id.manometro)
        canvass = Canvass(this, 230f, false)
        white = Canvass(this, 180f, true)
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


        val graph = findViewById<View>(R.id.graph) as GraphView
        series = LineGraphSeries()
        graph.addSeries(series)
        viewport = graph.viewport
        viewport.isYAxisBoundsManual = true
        viewport.setMinX(0.0)
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
        button_cancel_scan.setOnClickListener { onClick(it) }
        button_finish.setOnClickListener { onClick(it) }

    }

    private fun onClick(v: View) {
        when (v.id) {
            R.id.button_cancel_scan -> {
                Log.d(_tag, "Activity finish")
                btConnection.disconnect()
                finish()
            }
            R.id.button_finish -> {
                finish_scan()
            }
        }
    }

    private fun finish_scan() {
        btConnection.disconnect()
        var finishTime = (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - runtime).toDouble()
        viewport.setMaxX(finishTime)
        viewport.setMinX(0.0)
        val image: ByteArray = Converters.toByteArray(graph.takeSnapshot())

        var count = 0
        var stable_num = 0
        var prev: Int = 0
        for (i in 0 until pressure.size -1) {
            if (pressure[i] > 100 && count > 20) {
                stable_num = i
                break
            }
            if (pressure[i].toInt() == prev) {
                count += 1
            } else {
                count = 0
                prev = pressure[i].toInt()
            }
        }

        val downPressure = pressure.subList(stable_num, pressure.size - 1)
        val downTime = time_measure.subList(stable_num, time_measure.size - 1)
        Log.d("StablePress", downPressure[0].toString())
        Log.d("StableTime", downTime[0].toString())

        result = calculate(this, downPressure,  downTime)
        val currentDate = sdf.format(Date())
        val avg = (result[0] * 2 + result[1]) / 3
        Log.d(_tag, result.toString() )

        //El scan que se crea con los datos
        val scan = Scan(brazo = true, idManual = "", pressureAvg = avg, pressureSystolic = result[1], pressureDiastolic = result[0], scanDate = currentDate, pressureSystolicManual = result[1], pressureDiastolicManual = result[0], pressureAvgManual = 0.0, image = image)
        ioThread {
            val id = instanceDatabase.scanDao().insertScan(scan)
            val sc = instanceDatabase.scanDao().loadScanById(id)
            runOnUiThread() {
                val intent = Intent(this, DetailActivity::class.java)
                intent.putExtra("SCAN_KEY", sc)
                startActivityForResult(intent, 1)
            }

        }

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

    inner class Canvass(context: Context, var radius: Float, var white: Boolean) : View(context) {
        val paint = Paint()
        var textPaint = TextPaint().apply {
            textSize = 120f
        }

        override fun onDraw(canvas: Canvas) {
            val width = getWidth()
            val centerX = width.toFloat() / 2
            val centerY = height.toFloat() / 2
            if (white) {
                paint.setARGB(255, 250, 250, 255)
                canvas.drawCircle(centerX, centerY, this.radius, paint)
                canvas.drawText(pressureVal.toString(), centerX - 80, centerY + 40, textPaint)
            } else {
                paint.setARGB(255, 200, 200, 200)
                canvas.drawCircle(centerX, centerY, this.radius, paint)
            }
        }
    }

    inner class Needle(context: Context) : View(context) {
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


    }

    private fun updateValue(v: View?, newVal: Float) {
        var new_rotation = newVal
        if (new_rotation > 260f) {
            new_rotation = 280f
        } else if (new_rotation < 20f) {
            new_rotation = 20f
        }
        new_rotation += 50f

        ObjectAnimator.ofFloat(v, "rotation", new_rotation).start()
    }


    private fun addEntry(tiempo: Double, presion: Double) {

        series.appendData(DataPoint(tiempo, presion), true, 300)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        finish()
    }


    /**
     * starting listening service method
     */
    private fun startBTConnection(device: BluetoothDevice, uuid: UUID) {
        Log.d(_tag, "startBTConnection: Initializing RFCOM Bluetooth Connection.")


        val scanPoints = btConnection.startClient(device, uuid)
                .debounce(3, TimeUnit.MILLISECONDS)
                .map {
                    try {
                        var value = it.trim().replace("\\s".toRegex(), "").split(";")

                        if (value.size > 1) {
                            value
                        } else {
                            emptyList()
                        }
                    } catch (e: Exception) {

                        emptyList<String>()
                    }
                }
                .map {
                    try {
                        it[1].toDouble()
                        if (it[1].toDouble() > 180 || it[1].toDouble() < 20) {
                            emptyList<String>()
                        } else {
                            it
                        }
                    } catch (e: java.lang.Exception) {
                        emptyList<String>()
                    }

                }
                .filter { !it.isEmpty() }
                .map {
                    if (it[0].isEmpty() || it[1].isEmpty()) {
                        emptyList()
                    } else {
                        it
                    }
                }
                .filter { !it.isEmpty() }
                .map {
                    ScanPoint(it[0].toDouble(), it[1].toDouble())
                }
                .observeOn(AndroidSchedulers.mainThread())


        val disposable = scanPoints.subscribe {


            updateValue(needle, it.pressure.toFloat())
            var actual = (System.currentTimeMillis() - runtime)

//            Log.d(_tag, "time ${actual}")
//            Log.d(_tag, "time ${actual} ---- value ${it.pressure}")
            if (it.pressure <= 25) {
//                finish_scan()
            }

            addEntry(actual / 100.0, it.pressure)
            pressureVal = it.pressure.toInt()
            canvass.textPaint
            time_measure.add(actual)//it.time)
            pressure.add(it.pressure)

            if (it.pressure >= 100) {
                end_scan = true
            }

            white.invalidate()

            if (end_scan && it.pressure <= 25) {
                finish_scan()
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

    data class ScanPoint(val time: Double, val pressure: Double)//, val time: Double)
}