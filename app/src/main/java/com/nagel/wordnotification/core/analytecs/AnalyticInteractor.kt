package com.nagel.wordnotification.core.analytecs

import com.nagel.wordnotification.BuildConfig

object Analytic {

    private val analyticPlatforms: MutableList<AnalyticPlatform> = mutableListOf()

    fun setAnalyticPlatform(platform: AnalyticPlatform) {
        analyticPlatforms += platform
    }

    fun logEvent(
        eventType: String,
        eventName: String,
        eventValue: String?
    ) {
        logEvent(
            eventType = eventType,
            eventProperties = mapOf(eventName to eventValue)
        )
    }

    fun logEvent(
        eventType: String,
        eventProperties: Map<String, Any?> = emptyMap(),
        isFirebaseIncluded: Boolean = true
    ) {

        val filledEventProperties = getRequiredParameters()
            .plus(eventProperties)
            .mapValues { entry -> mapEmptyValues(entry.value) }

        analyticPlatforms
            .asSequence()
            .filterNot { platform -> !isFirebaseIncluded && platform is FirebaseAnalyticPlatform }
            .forEach { platform ->
                platform.logEvent(
                    eventType = eventType,
                    eventProperties = filledEventProperties
                )
            }
    }

    private fun mapEmptyValues(value: Any?) = when (value) {
        null, EVENT_PARAMETER_EMPTY -> ParametersAnalytics.NONE
        else -> value.toString().uppercase()
    }

    private fun getRequiredParameters() = mapOf(
//        ParametersAnalytics.DEVICE_ID to sharedPrefs.getDeviceId(),
//        ParametersAnalytics.USER_ID to sharedPrefs.getPrefUserIdOrNone(),
        ParametersAnalytics.PLATFORM to ParametersAnalytics.ANDROID,
        ParametersAnalytics.APP_VERSION to BuildConfig.VERSION_NAME,
//        ParametersAnalytics.SOURCE to sharedPrefs.getInstallationSource(),
    )

    private const val EVENT_PARAMETER_EMPTY = ""
}