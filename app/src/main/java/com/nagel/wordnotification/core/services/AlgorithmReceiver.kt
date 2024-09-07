package com.nagel.wordnotification.core.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.nagel.wordnotification.Constants.TAKE_AWAY
import com.nagel.wordnotification.Constants.TYPE
import com.nagel.wordnotification.Constants.TYPE_ANSWER
import com.nagel.wordnotification.Constants.TYPE_QUEST
import com.nagel.wordnotification.app.App


class AlgorithmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("CoroutineWorker:", "AlgorithmReceiver: start")
        val type = intent.getIntExtra(TYPE, 0)
        val json = intent.getStringExtra(TAKE_AWAY)
        val notificationDto = Utils.getDtoFromJson(context, intent)
        if (notificationDto != null) {
            if (type == TYPE_ANSWER) {
                val newIntent = Intent(context, AlarmReceiver::class.java)
                newIntent.putExtra(TAKE_AWAY, json)
                newIntent.putExtra(TYPE, TYPE_QUEST)
                context.sendBroadcast(newIntent)
            }

            Log.d("CoroutineWorker:", "notificationDto: $notificationDto")
            val alarmManager = ContextCompat.getSystemService(App.get(), AlarmManager::class.java)
            val intentAlarm = Intent(App.get(), AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                App.get(),
                notificationDto.uniqueId + notificationDto.step,
                intentAlarm,
                PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager?.cancel(pendingIntent)
        }
    }
}