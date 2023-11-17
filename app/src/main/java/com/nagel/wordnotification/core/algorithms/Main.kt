package com.nagel.wordnotification.core.algorithms

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

object Main {

    @JvmStatic
    fun main(args: Array<String>) {
//        val startInterval = "01:45"
//        val sIHour = startInterval.substring(0, startInterval.indexOf(':'))
//        val sIM = startInterval.substring(startInterval.indexOf(':') + 1)
//        val c = Calendar.getInstance()
//        c.time = Date()
//        val res = c.get(Calendar.MINUTE)
//        println(sIHour.toInt())
        check()
    }

    @JvmStatic
    fun check() {
        listOf(1, 2, 3, 4, 5).forEach lit@ {
            if (it == 3) return@lit // local return to the caller of the lambda, i.e. the forEach loop
            print(it)
        }
    }
}