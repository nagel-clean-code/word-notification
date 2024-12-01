package com.nagel.wordnotification.data.firbase.entity

data class CurrentPrices(
    val button1Price: Int,
    val button1Title: String,
    val button2Price: Int,
    val button2Title: String,
    val button3Price: Int,
    val button3Title: String,
) {
    constructor() : this(0, "", 0, "", 0, "")
}