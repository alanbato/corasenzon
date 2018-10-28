package com.edgardo.corasensor.activities

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.edgardo.corasensor.R
import com.edgardo.corasensor.Scan.Scan
import kotlinx.android.synthetic.main.row.view.*


class ScanAdapter(val scans: List<Scan>, val listener: CustomItemClickListener) : RecyclerView.Adapter<ScanAdapter.ScanViewHolder>() {

    lateinit var scan: Scan
    private var numberOfItems = scans.size

    override fun onCreateViewHolder(ViewGroup: ViewGroup, p1: Int): ScanViewHolder {
        val scanViewHolder: ScanAdapter.ScanViewHolder

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

        //Assign data

        fun bind(index: Int) {
            scan = scans[index]

            itemView.text_id.text = scan._id.toString()
            itemView.text_date.text = scan.scanDate
            itemView.text_pressure.text = "${scan.pressureSystolic} / ${scan.pressureDiastolic} "


        }

        override fun onClick(p0: View?) {
            val scan = scans[adapterPosition]
            listener.onCustomItemClickListener(scan)
        }
    }

}
