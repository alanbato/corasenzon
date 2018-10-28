package com.edgardo.corasensor.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.edgardo.corasensor.R
import com.edgardo.corasensor.Scan.Scan
import com.edgardo.corasensor.database.ScanDataTest
import com.edgardo.corasensor.database.ScanDatabase
import com.edgardo.corasensor.networkUtility.Executor.Companion.ioThread
import com.edgardo.corasensor.scanData.ScanData
import kotlinx.android.synthetic.main.activity_scan_list.*

class ScanListActivity : AppCompatActivity() {
    private val scans: ArrayList<Scan> = ScanData().listaLibros

    lateinit var instanceDatabase: ScanDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_list)

        val layoutManager = LinearLayoutManager(this)
        recycler_view_list_scans.layoutManager = layoutManager



        ioThread {
            val scanNum = instanceDatabase.scanDao().getAnyScan()
            if (scanNum == 0) {
                insertScans()
            } else {
                loadAllScans()
            }
        }


    }
    // Start -> set initial data
    private fun insertScans() {
        val scan_list: List<Scan> = ScanDataTest(applicationContext).scanList
        ioThread {
            instanceDatabase.scanDao().insertScan(scan_list)
            loadAllScans()
        }
    }

    private fun loadAllScans() {
        ioThread {
            val scans = instanceDatabase.scanDao().loadAllScan()
            scans.observe(this, Observer<List<Scan>> { scans ->

            })

        }
    }


}
interface CustomItemClickListener {
    fun onCustomItemClickListener(scan: Scan)
}
