package com.nagel.wordnotification.core.algorithms

import android.content.Context
import com.nagel.wordnotification.R

object ForgetfulnessCurveLong : Algorithm {

    private val mapDate = mapOf<Int, Long>(
        1 to 1 * 60,
        2 to 30 * 60,
        3 to 24 * 60 * 60,
        4 to 21 * 24 * 60 * 60,
        5 to 6 * 30 * 24 * 60 * 60,
    )

    override fun getNewDate(lastStep: Int, currentTime: Long): Long? {
        val move = mapDate[lastStep + 1]
        return if (move != null) {
            currentTime + (move * 1000)
        } else {
            null
        }
    }

    override fun getCountSteps() = mapDate.size

    override fun getName(context: Context): String {
        return context.getString(R.string.forgetfulness_curve_long)
    }
}