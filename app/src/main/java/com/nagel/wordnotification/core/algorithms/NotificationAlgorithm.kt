package com.nagel.wordnotification.core.algorithms

import android.util.Log
import com.nagel.wordnotification.core.services.NotificationDto
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.data.settings.SettingsRepository
import com.nagel.wordnotification.data.settings.entities.SelectedMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
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

    suspend fun start() {
        Log.d("CoroutineWorker:", "start")
        wordsForNotifications.emit(null)
        bufArray = arrayListOf()
        loadWords()
    }

    private fun loadWords() {
        Log.d("CoroutineWorker:", "loadWords")
        CoroutineScope(Dispatchers.IO).launch {
            val accountId = sessionRepository.getSession()?.account?.id
            Log.d("CoroutineWorker:", "accountId:${accountId}")
            accountId?.let { id ->
                dictionaryRepository.loadDictionaries(id).collect() { dictionaries ->
                    Log.d("CoroutineWorker:", "dictionariesSize:${dictionaries.size}")
                    dictionaries.forEach {
                        if (it.include) {
                            setupNotification(it)
                        }
                    }
                    wordsForNotifications.emit(bufArray.toList())
                }
            }
        }
    }

    private suspend fun setupNotification(dictionary: Dictionary) {
        val mode = settingsRepository.getModeSettings(dictionary.idDictionaries) ?: return
        when (mode.selectedMode) {
            SelectedMode.PlateauEffect.toString() -> {
                plateauEffect(dictionary)
            }

            SelectedMode.ForgetfulnessCurveLong.toString() -> {
//                plateauEffect(dictionary)
            }

            SelectedMode.ForgetfulnessCurve.toString() -> {
//                plateauEffect(dictionary)
            }
        }

    }

    //TODO нужно учитывать выбранные дни недели и время
    private var countFirstNotifications = 0
    private suspend fun plateauEffect(dictionary: Dictionary) {
        bufArray.addAll(
            dictionary.wordList.filter { !it.learned && !it.active }.map {
                if (it.learnStep == 0) {
                    it.lastDateMention += countFirstNotifications++ * 5 * 60
                }
                val nextTimeNotification =
                    AlgorithmPlateauEffect.getNewDate(it.learnStep++, it.lastDateMention)
                val d = if (nextTimeNotification == null) {
                    it.learned = true
                    null
                } else {
                    it.lastDateMention = nextTimeNotification
                    NotificationDto(it.textFirst, it.textLast, nextTimeNotification, it.uniqueId)
                }
                updateWord(it)
                d
            }
        )
    }

    private suspend fun updateWord(word: Word){
        dictionaryRepository.updateWord(word)
    }
}