package com.nagel.wordnotification.data.premium

interface PremiumRepository {
    fun getAddNumberFreeRandomizer(): Int
    fun saveAddNumberFreeRandomizer(number: Int)

    fun getAddNumberFreeWords(): Int
    fun saveAddNumberFreeWords(number: Int)

    fun getMinFreeRandomizer(): Int
    fun saveMinFreeRandomizer(number: Int)

    fun getMinFreeWords(): Int
    fun saveMinFreeWords(number: Int)

    fun saveIsStarted(isStarted: Boolean)
    fun getIsStarted(): Boolean

    fun getCurrentLimitWord(): Int
    fun saveCurrentLimitWords(limit: Int)

    fun getCurrentLimitRandomizer(): Int
    fun saveCurrentLimitRandomizer(limit: Int)

    fun saveAllIsStarted(endTime: Long)
}