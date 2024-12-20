package com.nagel.wordnotification.core.services

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
import com.nagel.wordnotification.Constants
import com.nagel.wordnotification.Constants.NOTIFICATION_CHANNEL_ID
import com.nagel.wordnotification.Constants.TAKE_AWAY
import com.nagel.wordnotification.Constants.TYPE
import com.nagel.wordnotification.Constants.TYPE_ANSWER
import com.nagel.wordnotification.Constants.TYPE_QUEST
import com.nagel.wordnotification.R


class AlarmReceiver : BroadcastReceiver() {

    private var currentType = 1

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("CoroutineWorker", "Intent.extras: ${intent.extras}")
        val notificationDto = Utils.getDtoFromJson(context, intent)
        currentType = intent.getIntExtra(TYPE, TYPE_ANSWER)
        notificationDto?.let {
            Log.d("CoroutineWorker", "Сработал ${notificationDto.text}")
            newNotification(context, notificationDto)
        } ?: kotlin.run {
            val json = intent.getStringExtra(TAKE_AWAY)
            Log.d("Json:", json.toString())
            Log.d("currentType:", currentType.toString())
            Utils.showError(context, "notificationDto = null")
        }
    }

    private fun newNotification(context: Context, dto: NotificationDto) {
        val pendingIntent = if (currentType == TYPE_ANSWER) {
            getAction(context, dto, TYPE_QUEST)
        } else {
            getAction(context, dto, TYPE_ANSWER)
        }

        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nc = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                context.resources.getString(R.string.app_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            if (currentType == TYPE_ANSWER) {
                val customSoundUri =
                    Uri.parse("android.resource://${context.packageName}/${R.raw.custom_sound}")
                val attributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                nc.setSound(customSoundUri, attributes)
            }
            notificationManager.createNotificationChannel(nc)
        }

        val customNotification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.arrow)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)

        val content = context.getString(R.string.memorization_step) + " ${dto.step + 1}"
        if (currentType == TYPE_ANSWER) {
            customNotification
                .setContentTitle(dto.text)
                .setContentText(content)
                .setDeleteIntent(getAction(context, dto, TYPE_QUEST))
                .addAction(
                    0,
                    context.getString(R.string.show_answer),
                    getAction(context, dto, TYPE_ANSWER)
                )
        }
        if (currentType == TYPE_QUEST) {
            customNotification
                .setContentTitle("${dto.text} - ${dto.translation}")
                .setContentText(content)
                .setDeleteIntent(getAction(context, dto, TYPE_QUEST))
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