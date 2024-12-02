package com.nagel.wordnotification.data.firbase.entity

data class FeatureToggles(
    val content: List<String>
) {
    constructor() : this(emptyList())

    fun merge(data: FeatureToggles): FeatureToggles {
        return FeatureToggles(data.content + content)
    }
}