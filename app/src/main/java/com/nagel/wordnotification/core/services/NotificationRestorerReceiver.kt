package com.nagel.wordnotification.core.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nagel.wordnotification.core.algorithms.NotificationAlgorithm
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationRestorerReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationAlgorithm: NotificationAlgorithm

    override fun onReceive(context: Context?, intent: Intent?) {
        val pendingResult = goAsync()
        GlobalScope.launch(Dispatchers.Default) {
            try {
                delay(DELAY_BEFORE_CREATING_NOTIFICATION)
                notificationAlgorithm.createNotification()
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val DELAY_BEFORE_CREATING_NOTIFICATION = 10_000L
    }
}