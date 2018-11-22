package com.edgardo.corasensor.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


import com.edgardo.corasensor.R
import com.edgardo.corasensor.activities.DetailActivity
import com.edgardo.corasensor.activities.ScanActivity
import kotlinx.android.synthetic.main.fragment_start_scan.*
import kotlinx.android.synthetic.main.fragment_start_scan.view.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class startScanFragment : Fragment(), View.OnClickListener  {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onClick(v0 : View?){
        val intent = Intent(context, ScanActivity::class.java)
        startActivity(intent)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var rootview = inflater.inflate(R.layout.fragment_start_scan, container, false)
        rootview.button_start.setOnClickListener(this)
        return rootview
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                startScanFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}
