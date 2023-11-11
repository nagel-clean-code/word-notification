package com.nagel.wordnotification.core.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.nagel.wordnotification.data.dictionaries.entities.Word


object Utils {

    fun deleteNotification(context: Context, word: Word) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        val myIntent = Intent(context, AlarmReceiver::class.java)
        for (i in (0..word.learnStep)) {
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                word.uniqueId + word.learnStep - i,
                myIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager?.cancel(pendingIntent)
        }
    }

    fun deleteNotification(context: Context, wordList: List<Word>) {
        wordList.forEach { word ->
            deleteNotification(context, word)
        }
    }

}