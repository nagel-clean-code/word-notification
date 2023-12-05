package com.nagel.wordnotification.core.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nagel.wordnotification.app.App
import com.nagel.wordnotification.core.algorithms.NotificationAlgorithm
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject


@HiltWorker
class AlgorithmAdjustmentWork @AssistedInject constructor(
    @Assisted val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private var algorithm: NotificationAlgorithm
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result { //TODO может ли CoroutineWorker выбросить ANR???
        Log.d("CoroutineWorker:doWork:", "start")
        val words = algorithm.getWords()
        Log.d("CoroutineWorker:", "words -> ${words.size}")
        words.forEach { word ->
            word?.let {
                startAlarm(it)
            }
        }
        return Result.success()
    }

    private fun startAlarm(word: NotificationDto) {
        Log.d("CoroutineWorker:startAlarm:", word.toString())
        val intent = Intent(App.get(), AlarmReceiver::class.java)
        intent.putExtra("TAKE_AWAY", word)
        val pendingIntent = PendingIntent.getBroadcast(
            App.get(),
            word.uniqueId + word.step,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        Log.d(
            "CoroutineWorker:startAlarm:",
            "requestCode: ${word.uniqueId + word.step}" + ", Name:${word.text}"
        )
        val alarmManager = ContextCompat.getSystemService(appContext, AlarmManager::class.java)
        alarmManager!!.setExact(AlarmManager.RTC_WAKEUP, word.date, pendingIntent)
    }

}