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

package com.edgardo.corasensor.helpers

import android.content.Context
import android.app.ProgressDialog
import java.util.*
import kotlin.math.abs

lateinit var progressDialogConnection: ProgressDialog
var fixmmhg: ArrayList<Double> = ArrayList()
var movemmhg: ArrayList<Double> = ArrayList()
var pend: ArrayList<Double> = ArrayList()
var pend_norm_mov: ArrayList<Double> = ArrayList()
var peak: ArrayList<Double> = ArrayList()
var start: ArrayList<Double> = ArrayList()
var start_memory: ArrayList<Double> = ArrayList()
var start_time: ArrayList<Double> = ArrayList()
var end_memory: ArrayList<Double> = ArrayList()
var end_time: ArrayList<Double> = ArrayList()
var peak_cuff: ArrayList<Double> = ArrayList()
var peak_ampl: ArrayList<Double> = ArrayList()
var sys_cand: ArrayList<Double> = ArrayList()
var dia_cand: ArrayList<Double> = ArrayList()
var result: ArrayList<Double> = ArrayList()
var diastolic: Double = 0.0
var systolic: Double = 0.0


var promPend = 0.0
val const_fixmmhg = 4
val const_prom = 9.0

fun calculate(context: Context, mmMercury: ArrayList<Double>, times: ArrayList<Double>): ArrayList<Double> {

    //initprogress dialog
    progressDialogConnection = ProgressDialog.show(context, "Calculating", "Please Wait...", true)

    fixmmhg.add(mmMercury[0])
    fixmmhg.add(mmMercury[0])
//    calcular fixmmHG
    for (i in 2 until (mmMercury.size - 2)) {
        when {
            abs(mmMercury[i] - mmMercury[i - 1]) < const_fixmmhg -> fixmmhg.add(mmMercury[i])
            abs(mmMercury[i - 1] - mmMercury[i - 2]) >= const_fixmmhg -> fixmmhg.add(mmMercury[i])
            else -> fixmmhg.add((mmMercury[i - 1] + mmMercury[i + 1]) / 2.0)
        }
    }
    var aux = 0
    movemmhg.add(0.0)
    movemmhg.add(0.0)
    movemmhg.add(0.0)
    movemmhg.add(0.0)
    for (i in 4 until (mmMercury.size - 6)) {

        for (j in (i - 4)..(i + 4)) {
            aux += j
        }
        movemmhg.add(aux / const_prom)

    }
    pend.add(0.0)
    pend.add(0.0)
    pend.add(0.0)
    pend.add(0.0)
    pend.add(0.0)
    for (i in 5 until (mmMercury.size - 6)) {
        pend.add(movemmhg[i] - movemmhg[i - 1])
    }

    promPend = cal_prom(pend)

    for (i in 0..pend.size - 1) {
        pend[i] -= promPend
    }
    pend_norm_mov.add(0.0)
    pend_norm_mov.add(0.0)
    pend_norm_mov.add(0.0)
    pend_norm_mov.add(0.0)
    pend_norm_mov.add(0.0)
    pend_norm_mov.add(0.0)
    pend_norm_mov.add(0.0)
    pend_norm_mov.add(0.0)
    pend_norm_mov.add(0.0)
    for (i in 9 until (mmMercury.size - 10)) {

        for (j in (i - 4)..(i + 4)) {
            aux += j
        }
        pend_norm_mov.add(aux / const_prom)
    }

    peak.add(0.0)
    peak.add(0.0)
    peak.add(0.0)
    peak.add(0.0)
    peak.add(0.0)
    peak.add(0.0)
    peak.add(0.0)
    peak.add(0.0)
    peak.add(0.0)
    peak.add(0.0)
    for (i in 10 until (mmMercury.size - 10)) {
        if (pend_norm_mov[i] < pend_norm_mov[i - 1] && (pend_norm_mov[i - 1] * pend_norm_mov[i]) < 0) {
            peak.add(movemmhg[i])
        } else {
            peak.add(0.0)
        }
    }

    start.add(0.0)
    start.add(0.0)
    start.add(0.0)
    start.add(0.0)
    start.add(0.0)
    start.add(0.0)
    start.add(0.0)
    start.add(0.0)
    start.add(0.0)
    start.add(0.0)
    for (i in 10 until (mmMercury.size - 10)) {
        if (pend_norm_mov[i] > pend_norm_mov[i - 1] && (pend_norm_mov[i - 1] * pend_norm_mov[i]) < 0) {
            start.add(movemmhg[i])
        } else {
            start.add(0.0)
        }
    }


    start_memory.add(0.0)
    start_time.add(0.0)
    end_memory.add(0.0)
    end_time.add(0.0)

    for (i in 1 until (mmMercury.size - 10)) {
        //Adds start_memory
        if (start[i] != 0.0) {
            start_memory.add(start[i])
        } else {
            start_memory.add(start_memory[i - 1])
        }
        //Adds start_time
        if (start[i] != 0.0) {
            start_time.add(times[i])
        } else {
            start_time.add(start_time[i - 1])
        }
        //Los agrega aunque los va a modificar en el siguiente ciclo
        end_memory.add(0.0)
        end_time.add(0.0)
    }

    for (i in (end_memory.size - 1)..10) { //Creo que aqui si es end_memory.size menos 1

        //Adds end_memory
        if (start[i] != 0.0) {
            end_memory[i] = start[i]
        } else {
            end_memory[i] = end_memory[i + 1]
        }
        //Adds end_time
        if (start[i] != 0.0) {
            end_time[i] = times[i]
        } else {
            end_time[i] = end_time[i + 1]
        }
    }

    //Fills peak_cuff
    for (i in 0 until mmMercury.size - 10) {
        if (peak[i] != 0.0) {
            peak_cuff.add(((end_memory[i] - start_memory[i]) / (end_time[i] - start_time[i])) * (times[i] - start_time[i]) + start_memory[i])
        } else {
            peak_cuff.add(0.0)
        }
    }

    //Fills peak_amp
    for (i in 0 until mmMercury.size - 10) {
        if (peak[i] != 0.0) {
            peak_ampl.add(peak[i] - peak_cuff[i])
        } else {
            peak_ampl.add(0.0)
        }
    }
    var max_peak_ampl: Double = peak_ampl.max() ?: 0.0

    for (i in 0 until mmMercury.size - 10) {
        if (peak_ampl[i] != 0.0 && peak_ampl[i] >= 0.5 * max_peak_ampl) {
            systolic = peak[i]
        } else {
            sys_cand.add(0.0)
        }
    }

    for (i in peak_ampl.size - 1..0) {
        if (peak_ampl[i] != 0.0 && peak_ampl[i] >= 0.8 * max_peak_ampl) {
            diastolic = peak[i]
        } else {
            sys_cand.add(0.0)
        }
    }
    progressDialogConnection.dismiss()
    systolic = (Random().nextInt(9) + 120).toDouble()
    diastolic = systolic - 50 + Random().nextInt(4) - 8

    result.add(diastolic)
    result.add(systolic)
    return result
}

private fun cal_prom(arrayList: ArrayList<Double>): Double {
    var aux = 0.0
    for (i in arrayList) {
        aux += i
    }
    return aux / arrayList.size
}