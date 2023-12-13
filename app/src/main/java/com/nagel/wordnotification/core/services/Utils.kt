package com.nagel.wordnotification.core.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.nagel.wordnotification.Constants
import com.nagel.wordnotification.app.App
import com.nagel.wordnotification.data.dictionaries.entities.Word


object Utils {

    fun deleteNotification(word: Word) {
        val context = App.get()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        val myIntent = Intent(context, AlarmReceiver::class.java)
        for (i in (1..word.learnStep)) {
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                word.uniqueId + i,
                myIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
            Log.d(
                "CoroutineWorker:delete:",
                "requestCode: ${word.uniqueId + i}" + ", name:${word.textFirst}"
            )
            alarmManager?.cancel(pendingIntent)
        }
    }

    fun deleteNotification(wordList: List<Word>) {
        wordList.forEach { word ->
            deleteNotification(word)
        }
    }

    fun getDtoFromJson(context: Context, intent: Intent): NotificationDto? {
        val json = intent.getStringExtra(Constants.TAKE_AWAY)
        return try {
            Gson().fromJson(json, NotificationDto::class.java)
        } catch (e: Exception) {
            showError(context, intent)
            null
        }
    }

    fun showError(context: Context, intent: Intent) {
        Log.d("CoroutineWorker", "Не удалось сериализовать: " + intent)
        Toast.makeText(context, "Уведомление не сработало", Toast.LENGTH_LONG).show()
    }
}