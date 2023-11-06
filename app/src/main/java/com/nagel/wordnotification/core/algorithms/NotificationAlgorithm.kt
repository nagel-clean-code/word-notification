package com.nagel.wordnotification.core.algorithms

import android.util.Log
import com.nagel.wordnotification.core.services.NotificationDto
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.data.settings.SettingsRepository
import com.nagel.wordnotification.data.settings.entities.SelectedMode
import com.nagel.wordnotification.data.settings.room.entities.ModeDbEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
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
        Log.d("CoroutineWorker:", "start")
        wordsForNotifications.emit(null)
        bufArray = arrayListOf()
        countFirstNotifications = 0
        loadWords()
    }

    private suspend fun loadWords() {
        Log.d("CoroutineWorker:", "loadWords")
        CoroutineScope(Dispatchers.IO).launch {
            val accountId = sessionRepository.getSession()?.account?.id
            Log.d("CoroutineWorker:", "accountId:${accountId}")
            accountId?.let { id ->
                dictionaryRepository.loadDictionaries(id).collect() { dictionaries ->
                    Log.d("CoroutineWorker:", "dictionariesSize:${dictionaries.size}")
                    dictionaries.forEach {
                        if (it.include) {
                            initNotifications(it)
                        }
                    }
                    wordsForNotifications.emit(bufArray.toList())
                }
            }
        }
    }

    //TODO нужно учитывать выбранные дни недели и время
    private suspend fun initNotifications(dictionary: Dictionary) {
        val mode = settingsRepository.getModeSettings(dictionary.idDictionaries)
        if (mode == null) {
            Log.d("CoroutineWorker:", "mode == null")
            return
        }
        dictionary.wordList.filter { !it.learned && it.lastDateMention < Date().time }.forEach {
            if (it.learnStep == 0) {
                //Добавление интервала между словами на первом шаге, чтобы не появились все в один раз
                it.lastDateMention = countFirstNotifications++ * getIntervalBetweenWords()
            }
            var nextTime = getNewDate(mode, it.learnStep++, it.lastDateMention)
            do {
                val d = if (nextTime == null) {
                    it.learned = true
                    null
                } else {
                    it.lastDateMention = nextTime
                    NotificationDto(
                        it.textFirst,
                        it.textLast,
                        nextTime,
                        it.uniqueId,
                        it.learnStep
                    )
                }
                updateWord(it)
                bufArray.add(d)
                nextTime = AlgorithmPlateauEffect.getNewDate(it.learnStep++, it.lastDateMention)
            } while (nextTime != null && nextTime - Date().time < MAX_WORKER_RESTART_INTERVAL)
        }
    }

    private fun getNewDate(mode: ModeDbEntity, step: Int, lastDate: Long): Long? {
        return when (mode.selectedMode) {
            SelectedMode.PlateauEffect::class.simpleName -> {
                AlgorithmPlateauEffect.getNewDate(step, lastDate)
            }

            SelectedMode.ForgetfulnessCurveLong::class.simpleName -> {
                AlgorithmForgetfulnessCurveLong.getNewDate(step, lastDate)
            }

            SelectedMode.ForgetfulnessCurve::class.simpleName -> {
                AlgorithmForgetfulnessCurve.getNewDate(step, lastDate)
            }

            else -> {
                null
            }
        }
    }

    private suspend fun updateWord(word: Word) {
        dictionaryRepository.updateWord(word)
    }

    private fun getIntervalBetweenWords() = (2..5).random() * 60 * 1000L

    companion object {
        const val MAX_WORKER_RESTART_INTERVAL = 20 * 60 * 1000L
    }
}