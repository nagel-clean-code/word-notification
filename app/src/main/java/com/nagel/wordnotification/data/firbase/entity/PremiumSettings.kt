package com.nagel.wordnotification.data.firbase.entity

data class PremiumSettings(
    val addNumberFreeRandomizer: Int,
    val addNumberFreeWords: Int,
    val minFreeRandomizer: Int,
    val minFreeWords: Int,
) {
    constructor() : this(0, 0, 0, 0)
}