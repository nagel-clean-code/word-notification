package com.nagel.wordnotification.core.algorithms

import android.content.Intent
import android.util.Log
import com.google.gson.Gson
import com.nagel.wordnotification.Constants.TAKE_AWAY
import com.nagel.wordnotification.Constants.TYPE
import com.nagel.wordnotification.Constants.TYPE_ANSWER
import com.nagel.wordnotification.app.App
import com.nagel.wordnotification.core.services.AlarmReceiver
import com.nagel.wordnotification.core.services.NotificationDto
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.data.settings.SettingsRepository
import com.nagel.wordnotification.data.settings.entities.ModeSettingsDto
import com.nagel.wordnotification.data.settings.room.entities.ModeDbEntity
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs


@Singleton
class NotificationAlgorithm @Inject constructor(
    private var sessionRepository: SessionRepository,
    private var settingsRepository: SettingsRepository,
    private var dictionaryRepository: DictionaryRepository
) {

    /**
     * @param wordNotification - на случай, если мы откатили текущее уведомление и запустили заново
     */
    suspend fun createNotification(wordNotification: Word? = null) {
        var word = prepareWord(wordNotification) ?: getNextWordNotification() ?: return
        Log.d("CoroutineWorker:", "Выбрано - слово: $word")

        if (word.nextDate == null) {
            Log.d("CoroutineWorker:", "Слово: ${word.textFirst} уже выучено")
            updateWord(word.markWordAsLearned())
            word = getNextWordNotification() ?: return
            Log.d("CoroutineWorker:", "Подобрал новое: ${word.textFirst}")
        }

        val mode = word.mode ?: return
        sessionRepository.saveCurrentWordNotification(word.idWord)
        val nextDate = AlgorithmHelper.nextAvailableDate(word.nextDate!!, mode.toMode())
        Log.d(
            "CoroutineWorker:",
            "word.nextDate: ${dateFormat.format(Date(word.nextDate!!))} || nextDate: ${
                dateFormat.format(Date(nextDate))
            }"
        )

        val notification = createNotificationDto(word, nextDate)
        if (nextDate < Date().time) {
            Log.d(
                "CoroutineWorker:",
                "nextDate: ${dateFormat.format(Date(nextDate))} || Date().time: ${
                    dateFormat.format(Date().time)
                }"
            )
            val appContext = App.get()
            val newIntent = Intent(appContext, AlarmReceiver::class.java)
            val json = Gson().toJson(notification)
            newIntent.putExtra(TAKE_AWAY, json)
            newIntent.putExtra(TYPE, TYPE_ANSWER)
            appContext.sendBroadcast(newIntent)
            Log.d("CoroutineWorker:", "sendBroadcast AlarmReceiver: not:${notification}")
        } else {
            Log.d("CoroutineWorker:", "createAlarm notification: ${notification}")
            AlgorithmHelper.createAlarm(notification)
        }
    }

    private suspend fun getNextWordNotification(): Word? {
        //Получаем все активные слова
        val accountId = sessionRepository.getAccountId() ?: return null
        val currentTime = Date().time
        return dictionaryRepository.loadDictionaries(accountId).filter {
            it.include && it.wordList.isNotEmpty()
        }.map { dic ->
            val mode = settingsRepository.getModeSettingsById(dic.idMode) ?: return null
            val dicWord = dic.wordList.filter {
                !it.allNotificationsCreated
            }.sortedBy { word ->
                val nextDate = getNewDate(mode, word.learnStep, word.getLastDateMentionOrNull())
                word.nextDate = nextDate
                word.mode = mode
                if (nextDate == null) { //Слово уже выучено
                    Log.d("CoroutineWorker:", "Фильтрация: ${word.textFirst} || Long.MAX_VALUE")
                    Long.MAX_VALUE
                } else {
                    Log.d(
                        "CoroutineWorker:",
                        "Фильтрация: ${word.textFirst} || Мин:${(nextDate - currentTime) / 1000 / 60} STEP: ${word.learnStep}"
                    )
                    nextDate - currentTime
                }
            }[0] //Берём слово с самой просроченной датой или самой ближайшей
            Log.d("CoroutineWorker:", "ВЫБРАЛИ DICWORD: ${dicWord.textFirst}")
            dicWord
        }.sortedBy {
            if (it.nextDate == null) {
                Long.MAX_VALUE
            } else {
                it.nextDate!! - currentTime
            }
        }.getOrNull(0)
    }

    private suspend fun getOldData(word: Word): Long {
        val list = dictionaryRepository.loadHistoryNotification(word.idWord, word.mode!!.idMode)
        return list?.maxOfOrNull { it.dateMention } ?: Date().time
    }

    private suspend fun prepareWord(word: Word?): Word? {
        word ?: return null
        val oldData = getOldData(word)
        return word.apply {
            nextDate = getNewDate(
                word.mode!!,    // mode уже должен быть готов в ModeSettingsVM
                word.learnStep,
                oldData
            )
        }
    }

    private fun getFreeTimeForNotification(
        words: List<Word>,
        minStartTime: Long,
        mode: ModeSettingsDto,
    ): Long {
        if (words.isEmpty()) return minStartTime
        var currentPosition = minStartTime
        var lastDate = -1L
        var arrayIx = 0

        fun getCurrentDate(): Long {
            return if (arrayIx < words.size) {
                words[arrayIx++].lastDateMention
            } else {
                lastDate
            }
        }

        while (true) {
            val currentDate = getCurrentDate()
            if (lastDate != -1L) {
                if (abs(currentPosition - lastDate) >= MINIMUM_DISTANCE &&
                    abs(currentPosition - currentDate) >= MINIMUM_DISTANCE
                ) {
                    if (AlgorithmHelper.checkOccurrenceInTimeInterval(currentPosition, mode)) {
                        return currentPosition
                    } else {
                        val nextDate = AlgorithmHelper.nextAvailableDate(currentPosition, mode)
                        val newWords = words.filter {
                            it.lastDateMention >= minStartTime - MINIMUM_DISTANCE
                        }
                        return getFreeTimeForNotification(newWords, nextDate, mode)
                    }
                }
                val newPos = lastDate + MINIMUM_DISTANCE
                if (newPos >= currentPosition && abs(currentDate - newPos) >= MINIMUM_DISTANCE) {
                    if (AlgorithmHelper.checkOccurrenceInTimeInterval(newPos, mode)) {
                        return newPos
                    } else {
                        val nextDate = AlgorithmHelper.nextAvailableDate(newPos, mode)
                        val newWords = words.filter {
                            it.lastDateMention >= minStartTime - MINIMUM_DISTANCE
                        }
                        return getFreeTimeForNotification(newWords, nextDate, mode)
                    }
                }
                if (currentDate + MINIMUM_DISTANCE > currentPosition) {
                    currentPosition = currentDate + MINIMUM_DISTANCE
                }
            }
            lastDate = currentDate
        }
    }

    private fun createNotificationDto(
        word: Word,
        nextTime: Long
    ): NotificationDto {
        with(word) {
            return NotificationDto(textFirst, textLast, nextTime, uniqueId, learnStep)
        }
    }

    private fun getNewDate(mode: ModeDbEntity, step: Int, oldData: Long? = null): Long? {
        val currentTime = oldData ?: Date().time
        val time = when (mode.selectedMode) {
            PlateauEffect::class.simpleName -> {
                PlateauEffect.getNewDate(step, currentTime)
            }

            ForgetfulnessCurveLong::class.simpleName -> {
                ForgetfulnessCurveLong.getNewDate(step, currentTime)
            }

            ForgetfulnessCurveShort::class.simpleName -> {
                ForgetfulnessCurveShort.getNewDate(step, currentTime)
            }

            else -> {
                null
            }
        }
        return time
    }

    private suspend fun updateWord(word: Word) {
        dictionaryRepository.updateWord(word)
    }


    companion object {
        val dateFormat = SimpleDateFormat("d, HH:mm:ss")

        const val MINIMUM_DISTANCE = 60 * 1000L
    }
}