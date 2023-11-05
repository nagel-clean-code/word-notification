package com.nagel.wordnotification.core.algorithms

import java.util.Date

object AlgorithmForgetfulnessCurveLong {

    private val mapDate = mapOf<Int, Long>(
        1 to 1 * 60,
        2 to 30 * 60,
        3 to 24 * 60 * 60,
        4 to 21 * 24 * 60 * 60,
        5 to 6 * 30 * 24 * 60 * 60,
    )

    fun getNewDate(lastStep: Int, lastDate: Long): Long? {
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
}