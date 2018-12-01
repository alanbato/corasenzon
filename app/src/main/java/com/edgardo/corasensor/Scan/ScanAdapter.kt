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

package com.edgardo.corasensor.Scan

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.edgardo.corasensor.R
import kotlinx.android.synthetic.main.row.view.*

class ScanAdapter(var scans: List<Scan>,
                  var listener: ((Scan) -> Unit)?) : RecyclerView.Adapter<ScanAdapter.ScanViewHolder>() {
    lateinit var scan: Scan
    private var numberOfItems = scans.size


    override fun onCreateViewHolder(ViewGroup: ViewGroup, p1: Int): ScanViewHolder {

        val scanViewHolder: ScanViewHolder
        val rowView = LayoutInflater.from(ViewGroup.context).inflate(R.layout.row, ViewGroup, false)

        scanViewHolder = ScanViewHolder(rowView)

        return scanViewHolder
    }

    override fun getItemCount(): Int {
        return numberOfItems
    }

    override fun onBindViewHolder(holder: ScanViewHolder, position: Int) {
        holder.bind(position)
    }


    inner class ScanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        init {
            itemView.setOnClickListener(this)
        }

        fun bind(index: Int) {
            scan = scans[index]

            itemView.text_date.text = "Date: " + scan.scanDate.toString()
            itemView.text_pressure.text = "Avg: " + "${scan.pressureSystolic} / ${scan.pressureDiastolic} "
            if(scan.idManual != ""){
                itemView.text_id.text = "Id: " + scan.idManual.toString()
            }
            else{
                itemView.text_id.text = "Id: " + scan._id.toString()
            }
        }

        override fun onClick(p0: View?) {
            val scan = scans[adapterPosition]
            listener?.invoke(scan)
        }
    }
}






