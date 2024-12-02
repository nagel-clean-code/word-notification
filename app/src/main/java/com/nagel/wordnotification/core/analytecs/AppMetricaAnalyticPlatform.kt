package com.nagel.wordnotification.core.analytecs

import android.content.Context
import com.nagel.wordnotification.data.session.SessionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.profile.Attribute
import io.appmetrica.analytics.profile.UserProfile
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AppMetricaAnalyticPlatform @Inject constructor(
    @ApplicationContext val context: Context,
    sessionRepository: SessionRepository
) {

    fun changeStatusNotification(permissionGranted: Boolean) {
        val userProfile = UserProfile.newBuilder()
            .apply(Attribute.notificationsEnabled().withValue(false))
            .build()
        AppMetrica.reportUserProfile(userProfile)
    }

    fun changeIsGmsBuild(isGms: Boolean) {
        val userProfile = UserProfile.newBuilder()
            .apply(Attribute.customBoolean("google_services").withValue(isGms))
            .build()
        AppMetrica.reportUserProfile(userProfile)
    }
}