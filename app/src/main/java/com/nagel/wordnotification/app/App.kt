package com.nagel.wordnotification.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.FirebaseApp
import com.nagel.wordnotification.BuildConfig
import com.nagel.wordnotification.R
import com.nagel.wordnotification.utils.CheckCountyEs
import com.yandex.mobile.ads.common.MobileAds
import dagger.hilt.android.HiltAndroidApp
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.PredefinedDeviceTypes
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        instance = this

        FirebaseApp.initializeApp(this)

        permissionAnalytic = CheckCountyEs.checkPermissionAnalytics(this)
        if (permissionAnalytic) {
            val apiKey = resources.getString(R.string.APP_METRICA_API_KEY)
            val config = AppMetricaConfig.newConfigBuilder(apiKey)
                .withAppVersion(BuildConfig.VERSION_NAME)
                .withDeviceType(PredefinedDeviceTypes.TABLET)
                .withLocationTracking(true)
                .build()
            AppMetrica.activate(this, config)
            AppMetrica.enableActivityAutoTracking(this)
        }
        MobileAds.initialize(this) {}
    }

    companion object {

        private lateinit var instance: App
        var permissionAnalytic: Boolean = false

        fun get(): App {
            return instance
        }
    }
}