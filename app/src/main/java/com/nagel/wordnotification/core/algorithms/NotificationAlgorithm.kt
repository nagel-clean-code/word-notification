package com.nagel.wordnotification.core.algorithms

import android.content.Intent
import android.util.Log
import com.google.gson.Gson
import com.nagel.wordnotification.Constants.TAKE_AWAY
import com.nagel.wordnotification.Constants.TYPE
import com.nagel.wordnotification.Constants.TYPE_ANSWER
import com.nagel.wordnotification.Constants.dateFormat
import com.nagel.wordnotification.app.App
import com.nagel.wordnotification.core.services.AlarmReceiver
import com.nagel.wordnotification.core.services.NotificationDto
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.data.settings.SettingsRepository
import com.nagel.wordnotification.data.settings.room.entities.ModeDbEntity
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class NotificationAlgorithm @Inject constructor(
    private var sessionRepository: SessionRepository,
    private var settingsRepository: SettingsRepository,
    private var dictionaryRepository: DictionaryRepository
) {

    /**
     * @param wordNotification - на случай, если мы откатили текущее уведомление и запустили заново
     */
    suspend fun createNotification() {
        var word = getNextWordNotification() ?: return
        Log.d("CoroutineWorker:", "Выбрано - слово: $word")

        while (word.nextDate == null) {
            Log.d("CoroutineWorker:", "Слово: ${word.textFirst} уже выучено")
            updateWord(word.markWordAsLearned())
            word = getNextWordNotification() ?: return
            Log.d("CoroutineWorker:", "Подобрал новое: ${word.textFirst}")
        }

        val mode = word.mode ?: return
        sessionRepository.saveCurrentWordNotification(word.idWord)

        val currentTime = Date().time
        var nextDate = AlgorithmHelper.nextAvailableDate(word.nextDate!!, mode.toMode())
        Log.d(
            "CoroutineWorker:",
            "word.nextDate: ${dateFormat.format(Date(word.nextDate!!))} || nextDate: ${
                dateFormat.format(Date(nextDate))
            }"
        )

        if (nextDate <= currentTime) {
            nextDate = AlgorithmHelper.nextAvailableDate(currentTime, mode.toMode())
            Log.d(
                "CoroutineWorker:",
                "nextDate: ${dateFormat.format(Date(nextDate))} || Date().time: ${
                    dateFormat.format(Date().time)
                }"
            )
            val notification = createNotificationDto(word, nextDate)
            val appContext = App.get()
            val newIntent = Intent(appContext, AlarmReceiver::class.java)
            val json = Gson().toJson(notification)
            newIntent.putExtra(TAKE_AWAY, json)
            newIntent.putExtra(TYPE, TYPE_ANSWER)
            appContext.sendBroadcast(newIntent)
            Log.d("CoroutineWorker:", "sendBroadcast AlarmReceiver: not:${notification}")
        } else {
            val notification = createNotificationDto(word, nextDate)
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
            }.map { word ->
                var nextDate = getNewDate(mode, word.learnStep, word.getLastDateMentionOrNull())
                nextDate = if (nextDate != null) {
                    AlgorithmHelper.nextAvailableDate(nextDate, mode.toMode())
                } else {
                    null
                }
                word.apply {
                    this.nextDate = nextDate
                    this.mode = mode
                }
            }.sortedBy { word ->
                Log.d(
                    "CoroutineWorker:",
                    "Фильтрация: ${word.textFirst} || Мин:${((word.nextDate ?: Long.MAX_VALUE) - currentTime) / 1000 / 60} STEP: ${word.learnStep}"
                )
                (word.nextDate ?: Long.MAX_VALUE) - currentTime
            }.getOrNull(0) //Берём слово с самой просроченной датой или самой ближайшей
            Log.d(
                "CoroutineWorker:",
                "ВЫБРАЛИ в словаре ${dic.name}: ${dicWord?.textFirst} nextDate:${dicWord?.nextDate}"
            )
            dicWord
        }.sortedBy {
            it ?: return@sortedBy null
            if (it.nextDate == null) {
                Long.MAX_VALUE
            } else {
                it.nextDate!! - currentTime
            }
        }.getOrNull(0)
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
}