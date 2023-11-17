package com.nagel.wordnotification.core.algorithms

interface Algorithm {
    fun getNewDate(lastStep: Int, lastDate: Long): Long?
    fun getCountSteps(): Int
}