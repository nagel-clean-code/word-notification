package com.nagel.wordnotification.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.FirebaseApp
import com.nagel.wordnotification.R
import dagger.hilt.android.HiltAndroidApp
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig
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

        val apiKey = resources.getString(R.string.APP_METRICA_API_KEY)
        val config = AppMetricaConfig.newConfigBuilder(apiKey).build()
        AppMetrica.activate(this, config)
    }

    companion object {

        private lateinit var instance: App

        fun get(): App {
            return instance
        }
    }
}