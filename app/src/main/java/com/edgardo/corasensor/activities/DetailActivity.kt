package com.edgardo.corasensor.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import com.edgardo.corasensor.R
import com.edgardo.corasensor.Scan.Scan
import com.edgardo.corasensor.database.ScanDatabase
import com.edgardo.corasensor.networkUtility.Executor
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var extras: Bundle
    lateinit var instanceDatabase: ScanDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        extras = intent.extras ?: return
        instanceDatabase = ScanDatabase.getInstance(this)

        val scanRec = extras.getParcelable<Scan>(MainActivity.SCAN_KEY)
        text_pressure_systolic.setText(scanRec!!.pressureSystolic.toString())
        text_pressure_diastolic.setText(scanRec.pressureDiastolic.toString())
        text_presure_avg.setText(scanRec.pressureAvg.toString())
        text_systolic_manual.setText(scanRec.pressureSystolicManual.toString())
        text_diastolic_manual.setText(scanRec.pressureDiastolicManual.toString())
        text_avg_manual.setText(scanRec.pressureAvgManual.toString())
        text_identifier.setText(scanRec.idManual)
        switch_brazo.isChecked = scanRec.brazo!!

        disableEditText(text_pressure_systolic)
        disableEditText(text_pressure_diastolic)
        disableEditText(text_presure_avg)
        
        button_dont_save.setOnClickListener {
            Executor.ioThread {
                instanceDatabase.scanDao().updateScan(scanRec)
                runOnUiThread {
                    Toast.makeText(applicationContext, applicationContext.getString(R.string.dont_save), Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }

        button_save.setOnClickListener {
            Executor.ioThread {
                scanRec.pressureSystolicManual = text_systolic_manual.text.toString().toDouble()
                scanRec.pressureDiastolicManual = text_diastolic_manual.text.toString().toDouble()
                scanRec.pressureAvgManual = text_avg_manual.text.toString().toDouble()
                scanRec.brazo = switch_brazo.isChecked
                scanRec.idManual = text_identifier.text.toString()

                instanceDatabase.scanDao().updateScan(scanRec)
                runOnUiThread {
                    Toast.makeText(applicationContext, applicationContext.getString(R.string.save), Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }
    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun disableEditText(editText: EditText) {
        editText.isFocusable = false
        editText.isEnabled = false
        editText.isCursorVisible = false
        editText.keyListener = null
    }

}
