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
import com.nagel.wordnotification.core.NotificationAlgorithm
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar


@HiltWorker
class AlgorithmAdjustmentWork @AssistedInject constructor(
    @Assisted val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private var algorithm: NotificationAlgorithm
) : CoroutineWorker(appContext, workerParams) {

    init {
        CoroutineScope(Dispatchers.Main).launch {
            algorithm.wordsForNotifications.collect() { words ->
                Log.d("CoroutineWorker:", "words -> ${words?.size}")
                words?.forEach {
                    startAlarm(it)
                }
            }
        }
    }

    override suspend fun doWork(): Result {
        algorithm.start()
        delay(5 * 60 * 1000)
        return Result.success()

    }

    private fun startAlarm(word: NotificationDto) {
        Log.d("CoroutineWorker:", word.toString())
        val notifyTime: Calendar = Calendar.getInstance()
        notifyTime.set(Calendar.HOUR_OF_DAY, 12)
        notifyTime.set(Calendar.MINUTE, 0)
        notifyTime.set(Calendar.SECOND, 0)

        val intent = Intent(appContext, AlarmReceiver::class.java)
        intent.putExtra("TAKE_AWAY", word)
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            Math.random().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = ContextCompat.getSystemService(appContext, AlarmManager::class.java)
        alarmManager!!.set(AlarmManager.RTC_WAKEUP, word.date, pendingIntent)
        Log.d("CoroutineWorker:", "End alarm")
    }

}