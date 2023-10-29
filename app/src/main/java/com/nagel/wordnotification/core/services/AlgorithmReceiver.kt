package com.nagel.wordnotification.core.services

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import com.nagel.wordnotification.Constants.TAKE_AWAY
import com.nagel.wordnotification.Constants.TYPE
import com.nagel.wordnotification.Constants.TYPE_ANSWER
import com.nagel.wordnotification.Constants.TYPE_QUEST
import com.nagel.wordnotification.Constants.UNIQUE_NOTIFICATION_ID


class AlgorithmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
        manager?.cancel(UNIQUE_NOTIFICATION_ID)
        /**
         * Здесь нужно записать в sharedprefs в список очереди на обработку алгоритмом
         */
        val type = intent.getIntExtra(TYPE, 0)
        if (type == TYPE_ANSWER) {
            val notificationDto = intent.getParcelableExtra<NotificationDto>(TAKE_AWAY)
            notificationDto?.text = "Ответ"
            val newIntent = Intent(context, AlarmReceiver::class.java)
            newIntent.putExtra(TAKE_AWAY, notificationDto)
            newIntent.putExtra(TYPE, TYPE_QUEST)
            context.sendBroadcast(newIntent);
        }
    }
}