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

    lateinit var instanceDatabase: ScanDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_list)

        val layoutManager = LinearLayoutManager(this)
        recycler_view_list_scans.layoutManager = layoutManager
    }

}

interface CustomItemClickListener {
    fun onCustomItemClickListener(scan: Scan)
}
