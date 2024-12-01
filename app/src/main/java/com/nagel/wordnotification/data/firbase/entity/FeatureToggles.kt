package com.nagel.wordnotification.data.firbase.entity

data class FeatureToggles(
    val content: List<String>
) {
    constructor() : this(emptyList())
}