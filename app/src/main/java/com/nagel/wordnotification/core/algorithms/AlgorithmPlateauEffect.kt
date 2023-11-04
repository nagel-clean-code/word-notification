package com.nagel.wordnotification.core.algorithms

import java.util.Date

object AlgorithmPlateauEffect {

    private val mapDate = mapOf<Int, Long>(
        1 to 5 * 60,
        2 to 25,        //TODO нужно учесть циклы перезапуска воркера, чтобы не пропустить уведомление
        3 to 2 * 60,
        4 to 10 * 60,
        5 to 1 * 60 * 60,
        6 to 5 * 60 * 60,
        7 to 24 * 60 * 60,
        8 to 5 * 24 * 60 * 60,
        9 to 25 * 24 * 60 * 60,
        10 to 4 * 30 * 24 * 60 * 60,
    )

    fun getNewDate(lastStep: Int, lastDate: Long): Long? {
        val currentDate = if (lastDate == 0L) {
            Date().time
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