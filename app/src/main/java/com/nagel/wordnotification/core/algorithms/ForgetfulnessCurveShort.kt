package com.nagel.wordnotification.core.algorithms

import android.content.Context
import com.nagel.wordnotification.R
import java.util.Date

object ForgetfulnessCurveShort: Algorithm {

    private val mapDate = mapOf<Int, Long>(
        1 to 1 * 60,
        2 to 20 * 60,
        3 to 8 * 60 * 60,
        4 to 24 * 60 * 60,
    )

    override fun getNewDate(lastStep: Int, lastDate: Long): Long? {
        val currentDate = if (lastStep == 0) {
            Date().time + lastDate
        } else {
            lastDate
        }
        val move = mapDate[lastStep + 1]
        return if (move != null) {
            currentDate + (move * 1000)
        } else {
            null
        }
    }

    override fun getCountSteps(): Int = mapDate.size

    override fun getName(context: Context): String {
        return context.getString(R.string.forgetfulness_curve)
    }
}