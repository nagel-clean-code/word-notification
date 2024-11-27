package com.nagel.wordnotification.core.algorithms

import android.content.Context
import com.nagel.wordnotification.R

object ForgetfulnessCurveShort : Algorithm {

    private val mapDate = mapOf<Int, Long>(
        1 to 1 * 60,
        2 to 20 * 60,
        3 to 8 * 60 * 60,
        4 to 24 * 60 * 60,
    )

    private val mapDateText = mapOf(
        1 to R.string.in_one_minute,
        2 to R.string.in_20_minute,
        3 to R.string.after_8_hours,
        4 to R.string.after_1_day
    )

    override fun getNewDate(lastStep: Int, currentTime: Long): Long? {
        val move = mapDate[lastStep + 1]
        return if (move != null) {
            currentTime + (move * 1000)
        } else {
            null
        }
    }

    override fun getDateText(lastStep: Int): Int? = mapDateText[lastStep]

    override fun getCountSteps(): Int = mapDate.size

    override fun getName(context: Context): String {
        return context.getString(R.string.forgetfulness_curve)
    }
}