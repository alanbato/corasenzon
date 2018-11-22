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
            itemView.text_id.text = "Id: " + scan._id.toString()
            itemView.text_date.text = "Date: " + scan.scanDate.toString()
            itemView.text_pressure.text = "Avg: " + "${scan.pressureSystolic} / ${scan.pressureDiastolic} "
            itemView.text_systolic.text = "Sis: " + "${scan.pressureSystolic}"
            itemView.text_distolic.text = "Dias: " + "${scan.pressureDiastolic}"
        }

        override fun onClick(p0: View?) {
            val scan = scans[adapterPosition]
            listener?.invoke(scan)
        }
    }
}






