package com.edgardo.corasensor.activities


import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.edgardo.corasensor.HeartAssistantApplication
import com.edgardo.corasensor.R
import com.edgardo.corasensor.database.ScanDatabase
import com.edgardo.corasensor.networkUtility.BluetoothConnection
import com.edgardo.corasensor.networkUtility.BluetoothConnectionService
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_scan.*
import kotlinx.android.synthetic.main.activity_settings.*
import java.util.*


class ScanActivity : AppCompatActivity() {

    val _tag = "BT_SCAB"

    val bt_connect = BluetoothConnection(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)



        button_cancel.setOnClickListener { onClick(it) }
        button_finish.setOnClickListener { onClick(it) }

        val application = application
        if (application is HeartAssistantApplication) {
            application.scan?.subscribe {
                Log.d(_tag, it)
            }
        }
    }

    private fun onClick(v: View) {
        when (v.id) {
            R.id.button_cancel -> {
                bt_connect.onDestroy()
                finish()
            }
            R.id.button_finish -> {
                bt_connect.onDestroy()
                val intent = Intent(this, DetailActivity::class.java)
                startActivity(intent)
            }
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        bt_connect.onDestroy()
    }


}
