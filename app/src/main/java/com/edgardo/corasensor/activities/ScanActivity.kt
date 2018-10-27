package com.edgardo.corasensor.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.edgardo.corasensor.R
import kotlinx.android.synthetic.main.activity_scan.*
import android.content.Intent



class ScanActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

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
}
