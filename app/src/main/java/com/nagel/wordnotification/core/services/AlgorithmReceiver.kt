package com.nagel.wordnotification.core.services

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.util.Log
import com.nagel.wordnotification.Constants.TAKE_AWAY
import com.nagel.wordnotification.Constants.TYPE
import com.nagel.wordnotification.Constants.TYPE_ANSWER
import com.nagel.wordnotification.Constants.TYPE_QUEST
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AlgorithmReceiver : BroadcastReceiver() {

    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("CoroutineWorker:", "AlgorithmReceiver: start")
        val pendingResult = goAsync()
        GlobalScope.launch(Dispatchers.Default) {
            try {
                val type = intent.getIntExtra(TYPE, 0)
                val json = intent.getStringExtra(TAKE_AWAY)
                val notificationDto = Utils.getDtoFromJson(context, intent)
                if (notificationDto != null) {
                    if (type == TYPE_ANSWER) {
                        val newIntent = Intent(context, AlarmReceiver::class.java)
                        newIntent.putExtra(TAKE_AWAY, json)
                        newIntent.putExtra(TYPE, TYPE_QUEST)
                        context.sendBroadcast(newIntent)
                        return@launch
                    }
                        //TODO добавить обработку ответа
                    Log.d("CoroutineWorker:", "notificationDto: $notificationDto")
                    val manager =
                        context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
                    manager?.cancel(notificationDto.uniqueId + notificationDto.step)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}