package com.nagel.wordnotification.core.algorithms

import android.content.Context
import com.nagel.wordnotification.R

object PlateauEffect : Algorithm {

    private val mapDate = mapOf<Int, Long>(
        1 to 1 * 60,
        2 to 5 * 60,
        3 to 25,
        4 to 2 * 60,
        5 to 10 * 60,
        6 to 1 * 60 * 60,
        7 to 5 * 60 * 60,
        8 to 24 * 60 * 60,
        9 to 5 * 24 * 60 * 60,
        10 to 25 * 24 * 60 * 60,
        11 to 4 * 30 * 24 * 60 * 60,
    )

    private val mapDateText = mapOf(
        1 to R.string.in_one_minute,
        2 to R.string.in_5_minutes,
        3 to R.string.in_25_seconds,
        4 to R.string.in_2_minutes,
        5 to R.string.in_10_minutes,
        6 to R.string.after_1_hour,
        7 to R.string.after_5_hour,
        8 to R.string.after_1_day,
        9 to R.string.after_5_day,
        10 to R.string.after_25_day,
        11 to R.string.after_4_months
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
        return context.getString(R.string.plateau_effect)
    }
}