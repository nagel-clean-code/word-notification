package com.nagel.wordnotification.data.firbase.entity

data class CurrentVersionData(
    val link: String,
    val mandatory: Boolean,
    val mandatoryUpdates: List<Int>,
    val noUpdateNeeded: List<Int>,
    val optionalUpdates: List<Int>
) {
    constructor() : this("", false, emptyList(), emptyList(), emptyList())
}