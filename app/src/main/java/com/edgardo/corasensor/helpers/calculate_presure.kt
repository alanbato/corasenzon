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
import kotlin.math.abs
import android.util.Log
import java.util.Random

lateinit var progressDialogConnection: ProgressDialog



var promPend = 0.0
val const_fixmmhg = 4
val const_prom = 9.0

fun calculate(context: Context, mmMercury: List<Double>, times: List<Long>):  ArrayList<Double> {

    var fixmmhg: ArrayList<Double> = ArrayList()
    var movemmhg: ArrayList<Double> = ArrayList()
    var pend: ArrayList<Double> = ArrayList()
    var pend_norm_mov: ArrayList<Double> = ArrayList()
    var peak: ArrayList<Double> = ArrayList()
    var start: ArrayList<Double> = ArrayList()
    var start_memory: ArrayList<Double> = ArrayList()
    var start_time: ArrayList<Long> = ArrayList()
    var end_memory: ArrayList<Double> = ArrayList()
    var end_time: ArrayList<Long> = ArrayList()
    var peak_cuff: ArrayList<Double> = ArrayList()
    var peak_ampl: ArrayList<Double> = ArrayList()
    var sys_cand: ArrayList<Double> = ArrayList()
    var dia_cand: ArrayList<Double> = ArrayList()
    var result: ArrayList<Double> = ArrayList()
    var diastolic: Double = 0.0
    var systolic: Double = 0.0

    //initprogress dialog
    progressDialogConnection = ProgressDialog.show(context, "Calculating", "Please Wait...", true)

    fixmmhg.add(mmMercury[0])
    fixmmhg.add(mmMercury[1])
//    calcular fixmmHG
    for (i in 2 until (mmMercury.size - 1)) {
        when {
            abs(mmMercury[i] - mmMercury[i - 1]) < const_fixmmhg -> fixmmhg.add(mmMercury[i])
            abs(mmMercury[i - 1] - mmMercury[i - 2]) >= const_fixmmhg -> fixmmhg.add(mmMercury[i])
            else -> fixmmhg.add((mmMercury[i - 1] + mmMercury[i + 1]) / 2.0)
        }
    }
    var aux = 0.0
    movemmhg.add(0.0)
    movemmhg.add(0.0)
    movemmhg.add(0.0)
    movemmhg.add(0.0)
    for (i in 4 until (mmMercury.size - 5)) {
        aux = 0.0
        for (j in (i - 4)..(i + 4)) {
            aux += fixmmhg[j]
        }
        movemmhg.add(aux / const_prom)

    }
    pend.add(0.0)
    pend.add(0.0)
    pend.add(0.0)
    pend.add(0.0)
    pend.add(0.0)
    for (i in 5 until (mmMercury.size - 5)) {
        pend.add(movemmhg[i] - movemmhg[i - 1])
    }
    Log.d("PENDS", pend.toString())
    promPend = cal_prom(pend)
    Log.d("PROMPEND", promPend.toString())

    for (i in 0 until pend.size) {
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
    for (i in 9 until (mmMercury.size - 9)) {
        aux = 0.0
        for (j in (i - 4)..(i + 4)) {
            aux += pend[j]
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
            Log.d("PEAK ADD", movemmhg[i].toString())
        } else {
            peak.add(0.0)
        }
    }
    Log.d("PENDNORMMOV", pend_norm_mov.toString())
    Log.d("MMERCURY SIZE", mmMercury.size.toString())
    Log.d("TIMES SIZE", times.size.toString())
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

    Log.d("Start", start.toString())


    for (i in 0..9) {
        start_memory.add(0.0)
        start_time.add(0)
        end_memory.add(0.0)
        end_time.add(0)
        peak_cuff.add(0.0)
        peak_ampl.add(0.0)
        sys_cand.add(0.0)
        dia_cand.add(0.0)
    }

    for (i in 10 until (mmMercury.size - 10)) {
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
        end_time.add(0)
    }
    end_memory.add(0.0)
    end_time.add(0)
    Log.d("STARTSIZE", start.size.toString())
    Log.d("TIMESIZE", times.size.toString())
    Log.d("STARTMEMSIZE", start_memory.size.toString())
    Log.d("ENDMEMSIZE", end_memory.size.toString())
    for (i in (mmMercury.size - 11) downTo 10) { //Creo que aqui si es end_memory.size menos 1
        //Adds end_memory
        if (start[i] != 0.0) {
            end_memory[i] = start_memory[i]
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

    Log.d("STARTMEM", start_memory.toString())
    Log.d("ENDMEM", end_memory.toString())
    Log.d("STARTIME", start_time.toString())
    Log.d("ENDTIME", end_time.toString())
    //Fills peak_cuff
    for (i in 10 until mmMercury.size - 10) {
        if (peak[i] != 0.0) {
            peak_cuff.add(
                    (end_memory[i] - start_memory[i])
                    / (end_time[i] - start_time[i])
                    * (times[i] - start_time[i])
                    + start_memory[i]
            )
            Log.d("PEAK CUFF ADD", peak_cuff[peak_cuff.size-1].toString())
        } else {
            peak_cuff.add(0.0)
        }
    }

    //Fills peak_amp
    for (i in 10 until mmMercury.size - 10) {
        if (peak[i] != 0.0) {
            val ampl = peak[i] - peak_cuff[i]
            if(ampl >= 2.5)
            {
                peak_ampl.add(0.0)
            }
            else{
                peak_ampl.add(ampl)
            }

            Log.d("PEAKAMPLADD", (peak[i] - peak_cuff[i]).toString())
        } else {
            peak_ampl.add(0.0)
        }
    }
    val max_peak_ampl: Double = peak_ampl.max() ?: 0.0
    Log.d("MAXPEAKAMPL", max_peak_ampl.toString())

    for (i in 10 until mmMercury.size - 10) {
        if ((peak_ampl[i] != 0.0) && (peak_ampl[i] >=(0.5 * max_peak_ampl))) {
            Log.d("PEAKSYST", peak_ampl[i].toString() + peak[i].toString())
            systolic = peak[i]
            break
        }
    }

    for (i in peak_ampl.size - 1 downTo 10) {
        if (peak_ampl[i] != 0.0 && peak_ampl[i] >= 0.8 * max_peak_ampl) {
            Log.d("PEAKDIAST", peak_ampl[i].toString() + peak[i].toString())
            diastolic = peak[i]
            break
        }
    }
    progressDialogConnection.dismiss()

    result.add(diastolic)
    result.add(systolic)
    Log.d("DIAST", diastolic.toString())
    Log.d("SYSY", systolic.toString())

    Log.d("xzRESULTADOS", 1.0.toString())
    Log.d("xztimes", times.toString())
    Log.d("xzhgMercury", mmMercury.toString())
    Log.d("xzFixedMercury", fixmmhg.toString())
    Log.d("xzMerMov", movemmhg.toString())
    Log.d("xzPend", pend.toString())
    Log.d("xzPendNorm", pend_norm_mov.toString())
    Log.d("xzPeaks", peak.toString())
    Log.d("xzStart", start.toString())
    Log.d("xzSTARTMEMARR", start_memory.toString())
    Log.d("xzSTARTTimeARR", start_time.toString())
    Log.d("xzENDMEMARR", end_memory.toString())
    Log.d("xzENDTIMEARR", end_time.toString())
    Log.d("xzPEAKCUFF", peak_cuff.toString())
    Log.d("xzPEAKAMPLEE", peak_ampl.toString())


    return result
}

private fun cal_prom(arrayList: ArrayList<Double>): Double {
    var aux = 0.0
    for (i in arrayList) {
        aux += i
    }
    return aux / arrayList.size
}