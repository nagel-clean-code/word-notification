package com.nagel.wordnotification.core

import android.util.Log
import com.nagel.wordnotification.core.services.NotificationDto
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.data.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class NotificationAlgorithm @Inject constructor(
    var sessionRepository: SessionRepository,
    var settingsRepository: SettingsRepository,
    var dictionaryRepository: DictionaryRepository
) {

    val wordsForNotifications = MutableStateFlow<List<NotificationDto>?>(null)
    private lateinit var bufArray: ArrayList<NotificationDto>
    private var i = 0

    suspend fun start() {
        Log.d("CoroutineWorker:", "start")
        wordsForNotifications.emit(null)
        bufArray = arrayListOf()
        i = 0
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
                        getNotificationFromDictionary(it)
                    }
                    wordsForNotifications.emit(bufArray.toList())
                }
            }
        }
    }

    private suspend fun getNotificationFromDictionary(dictionary: Dictionary) {
//        val mode = settingsRepository.getModeSettings() //FIXME Вызывать для каждого словаря по ID
        dictionaryRepository.getWordsByIdDictionary(dictionary.idDictionaries)
        bufArray.addAll(dictionary.wordList.map {
            NotificationDto(it.textFirst, Date().time + (i++ * 1000).toLong())
        })
    }

}