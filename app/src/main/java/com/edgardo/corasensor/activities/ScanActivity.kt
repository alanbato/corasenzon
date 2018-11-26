package com.edgardo.corasensor.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.edgardo.corasensor.R
import kotlinx.android.synthetic.main.activity_scan.*
import android.content.Intent
import com.jjoe64.graphview.series.LineGraphSeries
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.Viewport
import com.jjoe64.graphview.series.DataPoint
import kotlin.concurrent.thread
import com.edgardo.corasensor.R.id.graph
import com.edgardo.corasensor.Scan.Scan

import com.edgardo.corasensor.database.ScanDatabase
import com.edgardo.corasensor.networkUtility.Executor.Companion.ioThread

import java.util.*


class ScanActivity : AppCompatActivity() {
    lateinit var instanceDatabase: ScanDatabase

    lateinit var series : LineGraphSeries<DataPoint>
    private var lastX : Double = 6.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
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

        button_cancel.setOnClickListener {
            finish()
        }
        button_finish.setOnClickListener { onClick(it) }
    }

    private fun addEntry(){
        val r = Random()
        val randomValueY = 20 + (100 - 20) * r.nextDouble()
        lastX = lastX + 0.02
        series.appendData(DataPoint(lastX, randomValueY ), true, 300)
    }



    override fun onResume() {
        super.onResume()
        Thread(Runnable {
            while (true) {
                runOnUiThread { addEntry() }
                try {
                    Thread.sleep(20)
                } catch (e: InterruptedException) {
                    // manage error ...
                }
            }
        }).start()
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
                //El scan que se crea con los datos
                val scan = Scan(brazo = true, idManual = "Prueba" ,pressureAvg = 100.0, pressureSystolic = 120.0, pressureDiastolic = 80.0, scanDate = "26/10/2018", pressureSystolicManual = 0.0, pressureDiastolicManual = 0.0, pressureAvgManual = 0.0)
                ioThread {
                    instanceDatabase.scanDao().insertScan(scan)
                }

                val intent = Intent(this, DetailActivity::class.java)
                intent.putExtra("SCAN_KEY", scan )
                startActivityForResult(intent,1)
            }
        }
    }
}









































