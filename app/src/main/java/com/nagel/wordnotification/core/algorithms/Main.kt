package com.nagel.wordnotification.core.algorithms

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        val startInterval = "01:45"
        val sIHour = startInterval.substring(0, startInterval.indexOf(':'))
        val sIM = startInterval.substring(startInterval.indexOf(':') + 1)
        val c = Calendar.getInstance()
        c.time = Date()
        val res = c.get(Calendar.MINUTE)
        println(sIHour.toInt())

    }

    @JvmStatic
    fun check(args: String) {

    }
}