package com.nagel.wordnotification.core.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.nagel.wordnotification.app.App
import com.nagel.wordnotification.data.dictionaries.entities.Word


object Utils {

    fun deleteNotification(word: Word) {
        val context = App.get()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        val myIntent = Intent(context, AlarmReceiver::class.java)
        for (i in (0..word.learnStep)) {
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                word.uniqueId + word.learnStep - i,
                myIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
            Log.d("CoroutineWorker:delete:", "requestCode: ${word.uniqueId + word.learnStep - i}" + ", name:${word.textFirst}")
            alarmManager?.cancel(pendingIntent)
        }
    }

    fun deleteNotification(wordList: List<Word>) {
        wordList.forEach { word ->
            deleteNotification(word)
        }
    }

}