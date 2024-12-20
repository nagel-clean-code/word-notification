package com.nagel.wordnotification.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.FirebaseApp
import com.nagel.wordnotification.core.adv.RewardedAdLoaderImpl
import com.nagel.wordnotification.core.analytecs.AppMetricaAnalytic
import com.nagel.wordnotification.utils.CountyUtils
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var rewardedAdLoaderImpl: RewardedAdLoaderImpl

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        instance = this

        FirebaseApp.initializeApp(this)

        permissionAnalytic = CountyUtils.checkPermissionAnalytics(this)
        AppMetricaAnalytic.init(this, permissionAnalytic)
        rewardedAdLoaderImpl.init()
    }

    companion object {

        private lateinit var instance: App
        var permissionAnalytic: Boolean = false

        fun get(): App {
            return instance
        }
    }
}