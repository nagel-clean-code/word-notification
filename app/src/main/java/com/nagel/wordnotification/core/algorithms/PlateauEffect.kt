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

    override fun getNewDate(lastStep: Int, currentTime: Long): Long? {
        val move = mapDate[lastStep + 1]
        return if (move != null) {
            currentTime + (move * 1000)
        } else {
            null
        }
    }

    override fun getCountSteps(): Int = mapDate.size

    override fun getName(context: Context): String {
        return context.getString(R.string.plateau_effect)
    }
}