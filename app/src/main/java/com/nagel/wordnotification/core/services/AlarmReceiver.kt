package com.nagel.wordnotification.core.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.nagel.wordnotification.Constants.NOTIFICATION_CHANNEL_ID
import com.nagel.wordnotification.Constants.TAKE_AWAY
import com.nagel.wordnotification.Constants.TYPE
import com.nagel.wordnotification.Constants.TYPE_ANSWER
import com.nagel.wordnotification.Constants.TYPE_QUEST
import com.nagel.wordnotification.R
import com.nagel.wordnotification.presentation.MainActivity


class AlarmReceiver : BroadcastReceiver() {

    private var currentType = 1

    override fun onReceive(context: Context, intent: Intent) {
        val notificationDto = Utils.getDtoFromJson(context, intent)
        currentType = intent.getIntExtra(TYPE, TYPE_ANSWER)
        notificationDto?.let {
            Log.d("CoroutineWorker", "Сработал ${notificationDto.text}")
            newNotification(context, notificationDto)
        } ?: kotlin.run {
            Utils.showError(context, intent)
        }
    }

    private fun newNotification(context: Context, dto: NotificationDto) {
        val pendingIntent = PendingIntent.getActivity(
            context,
            dto.uniqueId + dto.step,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nc = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                context.resources.getString(R.string.app_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            val customSoundUri =
                Uri.parse("android.resource://${context.packageName}/${R.raw.custom_sound}")
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            nc.setSound(customSoundUri, attributes)
            notificationManager.createNotificationChannel(nc)
        }

        val title = context.getString(R.string.memorization_step)
        val customNotification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.arrow)
            .setContentTitle(title + dto.step)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)

        if (currentType == TYPE_ANSWER) {
            customNotification
                .setContentText(dto.text)
                .addAction(
                    0,
                    context.getString(R.string.show_answer),
                    getAction(context, dto, TYPE_ANSWER)
                )
        }
        if (currentType == TYPE_QUEST) {
            customNotification
                .setContentText("${dto.text} - ${dto.translation}")
                .addAction(
                    0,
                    context.getString(R.string.ok),
                    getAction(context, dto, TYPE_QUEST)
                )
        }

        notificationManager.notify(dto.uniqueId + dto.step, customNotification.build())
    }

    private fun getAction(
        context: Context,
        word: NotificationDto,
        type: Int
    ): PendingIntent {
        val json = Gson().toJson(word)
        val snoozeIntent = Intent(context, AlgorithmReceiver::class.java).apply {
            putExtra(TAKE_AWAY, json)
            putExtra(TYPE, type)
        }

        return PendingIntent.getBroadcast(
            context,
            word.uniqueId + word.step,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

}