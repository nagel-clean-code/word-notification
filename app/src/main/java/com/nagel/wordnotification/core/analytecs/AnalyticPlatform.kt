package com.nagel.wordnotification.core.analytecs

interface AnalyticPlatform {

    fun logEvent(
        eventType: String,
        eventProperties: Map<String, String>
    )
}