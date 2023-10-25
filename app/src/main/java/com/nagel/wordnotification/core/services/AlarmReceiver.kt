package com.nagel.wordnotification.core.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationDto = intent.getParcelableExtra<NotificationDto>("TAKE_AWAY")
        notificationDto?.let {
            Log.d("CoroutineWorker", "Сработал ${notificationDto.text}")
        } ?: kotlin.run {
            Log.d("CoroutineWorker", "Не удалось сериализовать")
        }
        //TODO Показ уведомления
    }

}