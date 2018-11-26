package com.edgardo.corasensor.helpers

import android.content.Context
import android.app.ProgressDialog
import kotlin.math.abs

lateinit var progressDialogConnection: ProgressDialog
lateinit var fixmmhg: ArrayList<Double>
lateinit var movemmhg: ArrayList<Double>
lateinit var pend: ArrayList<Double>
lateinit var pend_norm_mov: ArrayList<Double>
lateinit var peek: ArrayList<Double>
lateinit var diastolica: ArrayList<Double>
var promPend = 0.0
val const_fixmmhg = 4
val const_prom = 9.0

fun calculate(context: Context, mmMercury: ArrayList<Double>) {

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
    for (i in 4 until (fixmmhg.size - 6)) {

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
    for (i in 5 until (movemmhg.size - 6)) {
        pend.add(movemmhg[i] - movemmhg[i - 1])
    }

    promPend = cal_prom(pend)

    for (i in 0..pend.size) {
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
    for (i in 9 until (pend.size - 10)) {

        for (j in (i - 4)..(i + 4)) {
            aux += j
        }
        pend_norm_mov.add(aux / const_prom)
    }

    peek.add(0.0)
    peek.add(0.0)
    peek.add(0.0)
    peek.add(0.0)
    peek.add(0.0)
    peek.add(0.0)
    peek.add(0.0)
    peek.add(0.0)
    peek.add(0.0)
    peek.add(0.0)
    for (i in 10 until (pend_norm_mov.size - 10)) {
        if (pend_norm_mov[i] < pend_norm_mov[i - 1] && (pend_norm_mov[i - 1] * pend_norm_mov[i]) < 0) {
            peek.add(movemmhg[i])
        } else {
            peek.add(movemmhg[i])
        }
    }




    progressDialogConnection.dismiss()

}

private fun cal_prom(arrayList: ArrayList<Double>): Double {
    var aux = 0.0
    for (i in arrayList) {
        aux += i
    }
    return aux / arrayList.size
}