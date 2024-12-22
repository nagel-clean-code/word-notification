package com.nagel.wordnotification.data.firbase.entity

data class CurrentPrices(
    val advantagesPremium: List<String>,
    val button1Price: Int,
    val button1Title: String,
    val button2Price: Int,
    val button2Sale20: Boolean,
    val button2Title: String,
    val button3Price: Int,
    val button3Title: String,
    val linkButton1: String,
    val linkButton2: String,
    val linkButton3: String,
    val linkGetPremiumButton: String,
    val privacyLink: String?,
    val recoverLink: String?,
    val conditionsLink: String?,
) {
    constructor() : this(
        listOf(),
        0,
        "",
        0,
        true,
        "",
        0,
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        ""
    )
}