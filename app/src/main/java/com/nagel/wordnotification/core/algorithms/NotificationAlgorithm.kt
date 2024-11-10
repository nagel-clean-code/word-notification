package com.nagel.wordnotification.core.algorithms

import com.nagel.wordnotification.core.services.NotificationDto
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.NotificationHistoryItem
import com.nagel.wordnotification.data.dictionaries.entities.Word
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
    private var settingsRepository: SettingsRepository,
    private var dictionaryRepository: DictionaryRepository
) {

    suspend fun createNotification(word: Word, idMode: Long) {
        val mode = settingsRepository.getModeSettingsById(idMode) ?: return
        val minStartTime = getNewDate(mode, ++word.learnStep)
        if (minStartTime == null) { //Слово уже выучено
            updateWord(word.markWordAsLearned())
            return
        }

        //Получаем все активные слова
        val words = dictionaryRepository.getAllWords().filter {
            it.currentDateMention > 0 && word.currentDateMention >= minStartTime - MINIMUM_DISTANCE
        }.sortedBy {
            it.currentDateMention
        }
        val nextTime = getFreeTimeForNotification(words, minStartTime, mode.toMode())
        val notification = createNotificationDto(word, mode, nextTime)
        AlgorithmHelper.createAlarm(notification)
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
                words[arrayIx++].currentDateMention
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
                            it.currentDateMention >= minStartTime - MINIMUM_DISTANCE
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
                            it.currentDateMention >= minStartTime - MINIMUM_DISTANCE
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

    private suspend fun createNotificationDto(
        word: Word,
        mode: ModeDbEntity,
        nextTime: Long
    ): NotificationDto {
        with(word) {
            word.currentDateMention = nextTime
            val historyItem = NotificationHistoryItem(0, idWord, nextTime, mode.idMode, learnStep)
            dictionaryRepository.saveNotificationHistoryItem(historyItem)
            return NotificationDto(textFirst, textLast, nextTime, uniqueId, learnStep)
        }
    }

    private fun getNewDate(mode: ModeDbEntity, step: Int): Long? {
        val currentTime = Date().time
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
        val dateFormat = SimpleDateFormat("d, hh:mm:ss")

        const val MINIMUM_DISTANCE = 60 * 1000L
    }
}