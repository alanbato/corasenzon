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

import android.arch.lifecycle.Observer
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
            instanceDatabase.scanDao().insertScanList(scan_list)
            loadAllScans()
        }
    }

    private fun loadAllScans() {
        ioThread {
            val scan = instanceDatabase.scanDao().loadAllScan()
            scan.observe(this, Observer<List<Scan>> { scans ->

            })
        }
    }

}

interface CustomItemClickListener {
    fun onCustomItemClickListener(scan: Scan)
}
