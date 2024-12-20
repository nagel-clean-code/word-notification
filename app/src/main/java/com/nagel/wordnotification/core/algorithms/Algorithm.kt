package com.nagel.wordnotification.core.algorithms

import android.content.Context

interface Algorithm {
    fun getNewDate(lastStep: Int, currentTime: Long): Long?
    fun getDateText(lastStep: Int): Int?
    fun getCountSteps(): Int
    fun getName(context: Context): String
}