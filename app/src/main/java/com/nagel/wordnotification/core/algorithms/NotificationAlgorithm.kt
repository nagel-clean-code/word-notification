package com.nagel.wordnotification.core.algorithms

import android.util.Log
import com.nagel.wordnotification.core.services.NotificationDto
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.dictionaries.entities.NotificationHistoryItem
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.data.settings.SettingsRepository
import com.nagel.wordnotification.data.settings.entities.ModeSettingsDto
import com.nagel.wordnotification.data.settings.room.entities.ModeDbEntity
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class NotificationAlgorithm @Inject constructor(
    private var sessionRepository: SessionRepository,
    private var settingsRepository: SettingsRepository,
    var dictionaryRepository: DictionaryRepository,
) {

    val wordsForNotifications = MutableStateFlow<List<NotificationDto?>?>(null)
    private lateinit var bufArray: ArrayList<NotificationDto?>
    private var countFirstNotifications = 0

    suspend fun start() {
        wordsForNotifications.emit(null)
        bufArray = arrayListOf()
        countFirstNotifications = 0
        loadWords()
    }

    private suspend fun loadWords() {
        sessionRepository.getSession()?.account?.id?.let { id ->
            dictionaryRepository.loadDictionaries(id).collect() { dictionaries ->
                dictionaries.forEach {
                    if (it.include) {
                        initNotifications(it)
                    }
                }
                wordsForNotifications.emit(bufArray.toList())
            }
        }
    }

    private suspend fun initNotifications(dictionary: Dictionary) {
        val mode = settingsRepository.getModeSettings(dictionary.idDictionary)
        if (mode == null) {
            Log.d("CoroutineWorker:", "mode == null")
            return
        }
        dictionary.wordList.filter { !it.allNotificationsCreated && it.lastDateMention < Date().time }
            .forEach { word ->
                if (word.learnStep == 0) {
                    //Добавление интервала между словами на первом шаге, чтобы не появились все в один раз
                    word.lastDateMention = countFirstNotifications++ * getIntervalBetweenWords()
                }
                Log.d(TAG, "Current word: $word")
                var nextTime = getNewDate(mode, word.learnStep++, word.lastDateMention)
                do {
                    val notification = createNotificationDto(word, mode, nextTime)
                    nextTime = if (nextTime == null ||
                        !AlgorithmHelper.checkOccurrenceInTimeInterval(nextTime, mode.toMode())
                    ) {
                        word.learnStep--
                        updateWord(word)
                        null
                    } else {
                        updateWord(word)
                        bufArray.add(notification)
                        getNewDate(mode, word.learnStep++, word.lastDateMention)
                    }
                    Log.d(TAG, "nextTime = $nextTime")
                } while (nextTime != null && nextTime - Date().time < MAX_WORKER_RESTART_INTERVAL)
            }
    }

    private suspend fun createNotificationDto(
        word: Word,
        mode: ModeDbEntity,
        nextTime: Long?
    ): NotificationDto? {
        return if (nextTime == null) {
            word.allNotificationsCreated = true
            null
        } else {
            word.lastDateMention = nextTime
            val historyItem =
                NotificationHistoryItem(0, word.idWord, nextTime, mode.idMode, word.learnStep)
            dictionaryRepository.saveNotificationHistoryItem(historyItem)
            NotificationDto(word.textFirst, word.textLast, nextTime, word.uniqueId, word.learnStep)
        }
    }

    private fun getNewDate(mode: ModeDbEntity, step: Int, lastDate: Long): Long? {
        val time = when (mode.selectedMode) {
            PlateauEffect::class.simpleName -> {
                Log.d("CoroutineWorker:", "AlgorithmPlateauEffect")
                PlateauEffect.getNewDate(step, lastDate)
            }

            ForgetfulnessCurveLong::class.simpleName -> {
                Log.d("CoroutineWorker:", "AlgorithmForgetfulnessCurveLong")
                ForgetfulnessCurveLong.getNewDate(step, lastDate)
            }

            ForgetfulnessCurveShort::class.simpleName -> {
                Log.d("CoroutineWorker:", "ForgetfulnessCurveShort")
                ForgetfulnessCurveShort.getNewDate(step, lastDate)
            }

            else -> {
                null
            }
        }
        Log.d(
            "CoroutineWorker:",
            "nextTime = ${time?.let { dateFormat.format(Date(time)) }}"
        )
        return time
    }

    private suspend fun updateWord(word: Word) {
        dictionaryRepository.updateWord(word)
    }


    companion object {
        val dateFormat = SimpleDateFormat("d, hh:mm:ss")

        const val MAX_WORKER_RESTART_INTERVAL = 20 * 60 * 1000L
        fun getIntervalBetweenWords() = (2..5).random() * 60 * 1000L
        const val TAG = "CoroutineWorker:"
    }
}