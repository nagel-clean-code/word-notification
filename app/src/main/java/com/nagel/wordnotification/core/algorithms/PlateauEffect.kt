package com.nagel.wordnotification.core.algorithms

import android.content.Context
import com.nagel.wordnotification.R
import java.util.Date

object PlateauEffect : Algorithm {

    private val mapDate = mapOf<Int, Long>(
        1 to 5 * 60,
        2 to 25,
        3 to 2 * 60,
        4 to 10 * 60,
        5 to 1 * 60 * 60,
        6 to 5 * 60 * 60,
        7 to 24 * 60 * 60,
        8 to 5 * 24 * 60 * 60,
        9 to 25 * 24 * 60 * 60,
        10 to 4 * 30 * 24 * 60 * 60,
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
        return context.getString(R.string.plateau_effect)
    }
}