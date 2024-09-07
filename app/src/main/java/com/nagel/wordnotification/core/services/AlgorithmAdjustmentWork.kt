package com.nagel.wordnotification.core.services

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nagel.wordnotification.core.algorithms.AlgorithmHelper
import com.nagel.wordnotification.core.algorithms.NotificationAlgorithm
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject


@HiltWorker
class AlgorithmAdjustmentWork @AssistedInject constructor(
    @Assisted val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private var algorithm: NotificationAlgorithm
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("CoroutineWorker:doWork:", "start")
        val words = algorithm.getWords()
        Log.d("CoroutineWorker:", "words -> ${words.size}")
        words.forEach { word ->
            word?.let {
                AlgorithmHelper.createAlarm(it)
            }
        }
        return Result.success()
    }
}