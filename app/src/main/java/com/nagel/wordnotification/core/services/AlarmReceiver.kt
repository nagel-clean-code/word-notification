package com.nagel.wordnotification.core.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.nagel.wordnotification.Constants.NOTIFICATION_CHANNEL_ID
import com.nagel.wordnotification.Constants.TAKE_AWAY
import com.nagel.wordnotification.Constants.TYPE
import com.nagel.wordnotification.Constants.TYPE_ANSWER
import com.nagel.wordnotification.Constants.TYPE_QUEST
import com.nagel.wordnotification.Constants.UNIQUE_NOTIFICATION_ID
import com.nagel.wordnotification.R
import com.nagel.wordnotification.presentation.MainActivity


class AlarmReceiver : BroadcastReceiver() {

    private var currentType = 1

    override fun onReceive(context: Context, intent: Intent) {
        val notificationDto = intent.getParcelableExtra<NotificationDto>(TAKE_AWAY)
        currentType = intent.getIntExtra(TYPE, TYPE_ANSWER)
        notificationDto?.let {
            Log.d("CoroutineWorker", "Сработал ${notificationDto.text}")
            newNotification(context, notificationDto)
        } ?: kotlin.run {
            Log.d("CoroutineWorker", "Не удалось сериализовать")
        }
    }

    private fun newNotification(context: Context, dto: NotificationDto) {
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nc = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "default",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(nc)
        }
        val customNotification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.arrow)
            .setContentTitle("Слово для запоминания")
            .setContentText(dto.text)
            .setContentIntent(pendingIntent)
        if (currentType == TYPE_ANSWER) {
            customNotification.addAction(
                R.drawable.baseline_casino_24,
                context.getString(R.string.show_answer),
                getAction(context, dto)
            )
        }
        if (currentType == TYPE_QUEST) {
            customNotification.addAction(
                R.drawable.baseline_casino_24,
                context.getString(R.string.ok),
                getAction(context, dto)
            )
        }

        notificationManager.notify(UNIQUE_NOTIFICATION_ID, customNotification.build())
    }

    private fun getAction(
        context: Context,
        word: NotificationDto
    ): PendingIntent {
        val snoozeIntent = Intent(context, AlgorithmReceiver::class.java).apply {
            putExtra(TAKE_AWAY, word)
            putExtra(TYPE, currentType)
        }

        return PendingIntent.getBroadcast(
            context,
            0,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

}