package com.nagel.wordnotification.data.firbase.entity

import com.google.firebase.Timestamp
import java.util.Date

data class PremiumSettings(
    val addNumberFreeRandomizer: Int,
    val addNumberFreeWords: Int,
    val minFreeRandomizer: Int,
    val minFreeWords: Int,
    val allPremium: Timestamp
) {
    constructor() : this(0, 0, 0, 0, Timestamp(Date()))
}