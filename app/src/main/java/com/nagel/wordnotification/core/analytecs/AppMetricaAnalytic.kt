package com.nagel.wordnotification.core.analytecs

import android.app.Activity
import android.app.Application
import android.content.Intent
import com.nagel.wordnotification.BuildConfig
import com.nagel.wordnotification.R
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.PredefinedDeviceTypes
import io.appmetrica.analytics.profile.Attribute
import io.appmetrica.analytics.profile.UserProfile


object AppMetricaAnalytic {

    private var permissionAnalytic: Boolean = false
    private val userProfile by lazy { UserProfile.newBuilder() }

    fun init(context: Application, permissionAnalytic: Boolean) {
        this.permissionAnalytic = permissionAnalytic
        if (permissionAnalytic) {
            val apiKey = context.resources.getString(R.string.APP_METRICA_API_KEY)
            val config = AppMetricaConfig.newConfigBuilder(apiKey)
                .withAppVersion(BuildConfig.VERSION_NAME)
                .withDeviceType(PredefinedDeviceTypes.TABLET)
                .withLocationTracking(true)
                .build()
            AppMetrica.activate(context, config)
            AppMetrica.enableActivityAutoTracking(context)
        }
    }

    fun changeStatusNotification(permissionGranted: Boolean) {
        if (permissionAnalytic.not()) return
        val userProfile = UserProfile.newBuilder()
            .apply(Attribute.notificationsEnabled().withValue(permissionGranted))
            .build()
        AppMetrica.reportUserProfile(userProfile)
    }

    fun setProfileAttribute(attr: ProfileAttributeBase) {
        if (permissionAnalytic.not()) return
        val build = userProfile.apply(createProfileAttribute(attr)).build()
        AppMetrica.reportUserProfile(build)
    }

    fun setListProfileAttribute(attr: List<ProfileAttributeBase>) {
        if (permissionAnalytic.not()) return
        attr.forEach { item ->
            userProfile.apply(createProfileAttribute(item))
        }
        AppMetrica.reportUserProfile(userProfile.build())
    }

    fun reportEvent(key: String, map: Map<String, Any?>? = null) {
        if (permissionAnalytic.not()) return
        if (map != null) {
            AppMetrica.reportEvent(key, map)
        } else {
            AppMetrica.reportEvent(key)
        }
    }

    fun reportAppOpen(activity: Activity) {
        if (permissionAnalytic.not()) return
        AppMetrica.reportAppOpen(activity)
    }

    fun reportAppOpen(intent: Intent) {
        if (permissionAnalytic.not()) return
        AppMetrica.reportAppOpen(intent)
    }

    fun setUserProfileID(idAuthorUUID: String) {
        if (permissionAnalytic.not()) return
        AppMetrica.setUserProfileID(idAuthorUUID)
    }
}