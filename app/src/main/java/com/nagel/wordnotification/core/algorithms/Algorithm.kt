package com.nagel.wordnotification.core.algorithms

import android.content.Context

interface Algorithm {
    fun getNewDate(lastStep: Int, lastDate: Long): Long?
    fun getCountSteps(): Int
    fun getName(context: Context): String
}