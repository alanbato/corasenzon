package com.edgardo.corasensor.activities

import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.edgardo.corasensor.HeartAssistantApplication
import com.edgardo.corasensor.R
import com.edgardo.corasensor.Scan.Scan
import com.edgardo.corasensor.database.ScanDataTest
import com.edgardo.corasensor.database.ScanDatabase
import com.edgardo.corasensor.fragments.scanListFragment
import com.edgardo.corasensor.fragments.startScanFragment
import com.edgardo.corasensor.networkUtility.Executor.Companion.ioThread
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_start_scan.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    val _tag = "MainApp"

    lateinit var instanceDatabase: ScanDatabase

    companion object {
        const val SCAN_KEY: String = "SCAN_KEY"
    }

    val scanListFragment = scanListFragment()
    val startScan = startScanFragment.newInstance("Uno", "DOS")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val application = application
        if (application is HeartAssistantApplication) {
            application.scan?.subscribe {
                Log.d(_tag, it)
            }
        }



        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        instanceDatabase = ScanDatabase.getInstance(this)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
//        ioThread {
//            val cant = instanceDatabase.scanDao().getAnyScan()
//            if(cant == 0 ){
//                insertScans()
//            }else{
//                loadScans()
//            }
//        }


        with(supportFragmentManager.beginTransaction()) {
            add(R.id.fragment_list_scan, startScan)
            commit()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)


    }

    private fun onClick(v: View) {

        when (v.id) {
            R.id.button_start -> {

            }
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_home -> {
                with(supportFragmentManager.beginTransaction()) {
                    replace(R.id.fragment_list_scan, startScan)
                    commit()
                }

            }
            R.id.nav_history -> {
                with(supportFragmentManager.beginTransaction()) {
                    replace(R.id.fragment_list_scan, scanListFragment)
                    commit()
                }

                ioThread {
                    val cant = instanceDatabase.scanDao().getAnyScan()
                    if (cant == 0) {
                        insertScans()
                    } else {
                        loadScans()
                    }
                    runOnUiThread {
                        supportActionBar!!.title = "Scans"
                        val scan = instanceDatabase.scanDao().loadAllScan()
                        scan.observe(this, Observer<List<Scan>> { scans ->
                            scanListFragment.scans = scans ?: emptyList()
                            scanListFragment.onScanClick = ::scanClickShow
                        })
                        scanListFragment.scanAdapter?.notifyDataSetChanged()
                    }
                }

            }
            R.id.nav_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun loadScans() {
        ioThread {
            val scan = instanceDatabase.scanDao().loadAllScan()
            scan.observe(this, Observer<List<Scan>> { scans ->
                scanListFragment.scans = scans ?: emptyList()
                scanListFragment.onScanClick = ::scanClickShow
            })
        }
    }

    fun scanClickShow(scan: Scan) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra(SCAN_KEY, scan)
        startActivity(intent)
    }

    fun insertScans() {
        val scans: List<Scan> = ScanDataTest(applicationContext).scanList
        ioThread {
            instanceDatabase.scanDao().insertScanList(scans)
            loadScans()
        }
    }
}
