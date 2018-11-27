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

package com.edgardo.corasensor.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.edgardo.corasensor.R
import com.edgardo.corasensor.Scan.Scan
import com.edgardo.corasensor.Scan.ScanAdapter

class scanListFragment : Fragment() {
    var onScanClick: ((Scan) -> Unit)? = null
        set(value) {
            field = value
            scanAdapter?.listener = value
        }

    var scans: List<Scan>
        get() = scanAdapter?.scans ?: emptyList()
        set(value) {
            scanAdapter = ScanAdapter(value, scanAdapter?.listener)

            with(view as RecyclerView) {
                adapter = scanAdapter
            }
        }

    var scanAdapter : ScanAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let{
            scans = it.getParcelableArrayList(SCAN_ARRAY)
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_scan_list, container, false)

        val scansList = scans

        if(scansList != null) {
            scanAdapter = ScanAdapter(scansList, onScanClick).apply {
                listener = onScanClick

            }
        }

        // Set the adapter
        if (rootView is RecyclerView) {
            with(rootView) {
                layoutManager = android.support.v7.widget.LinearLayoutManager(context)
                adapter = scanAdapter
            }
        }

        return rootView
    }

    companion object {
        const val SCAN_ARRAY = "scanarray"
        @JvmStatic
        fun newInstance(scans: ArrayList<Scan>) =
                scanListFragment().apply {
                    arguments = Bundle().apply {
                        putParcelableArrayList(SCAN_ARRAY, scans)
                    }
                }
    }

}
