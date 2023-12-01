package com.nagel.wordnotification.core.analytecs

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAnalyticPlatform @Inject constructor(
    @ApplicationContext val context: Context
) : AnalyticPlatform {

    init {
        Analytic.setAnalyticPlatform(this)
    }

    override fun logEvent(eventType: String, eventProperties: Map<String, String>) {
        FirebaseAnalytics.getInstance(context).logEvent(eventType, eventProperties)
    }

    private fun FirebaseAnalytics.logEvent(
        name: String,
        params: Map<String, String>
    ) = logEvent(name) { params.forEach { (key, value) -> param(key, value) } }
}