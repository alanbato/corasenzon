package com.edgardo.corasensor

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.row.view.*

class MedicionAdapter(var mediciones: List<Medicion>,
                   var listener: ((Medicion) -> Unit)?): RecyclerView.Adapter<MedicionAdapter.MedicionViewHolder>() {
    lateinit var medicion: Medicion
    private var numberOfItems = mediciones.size


    override fun onCreateViewHolder(ViewGroup: ViewGroup, p1: Int): MedicionViewHolder {

        val medicionViewHolder: MedicionViewHolder
        val rowView = LayoutInflater.from(ViewGroup.context).inflate(R.layout.row, ViewGroup, false)

        medicionViewHolder = MedicionViewHolder(rowView)

        return medicionViewHolder
    }

    override fun getItemCount(): Int {
        return numberOfItems
    }

    override fun onBindViewHolder(holder: MedicionViewHolder, position: Int) {
        holder.bind(position)
    }


    inner class MedicionViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        init{
            itemView.setOnClickListener(this)
        }
        fun bind(index: Int){
            medicion = mediciones[index]
            itemView.text_pres_dias_r.text = medicion.presionDiastolica.toString()
            itemView.text_pres_med_r.text = medicion.presionMedia.toString()
            itemView.text_pres_sis_r.text = medicion.presionSiastolica.toString()
        }

        override fun onClick(p0: View?){
            val mediciony = mediciones[adapterPosition]
            listener?.invoke(mediciony)
        }
    }
}






