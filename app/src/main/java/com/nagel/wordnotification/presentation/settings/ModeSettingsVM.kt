package com.nagel.wordnotification.presentation.settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nagel.wordnotification.core.algorithms.Algorithm
import com.nagel.wordnotification.core.algorithms.PlateauEffect
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.settings.SettingsRepository
import com.nagel.wordnotification.data.settings.entities.ModeSettingsDto
import com.nagel.wordnotification.presentation.base.BaseViewModel
import com.nagel.wordnotification.presentation.base.MutableLiveResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ModeSettingsVM @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val dictionaryRepository: DictionaryRepository,
) : BaseViewModel() {

    var idDictionary: Long = -1
    var selectedMode: Algorithm? = PlateauEffect
    val loadingMode = MutableStateFlow<ModeSettingsDto?>(null)
    var dictionary: Dictionary? = null
    val liveResult: MutableLiveResult<Unit> = MutableLiveData()

    suspend fun loadDictionary(idDictionary: Long) {
        withContext(Dispatchers.IO) {
            dictionary = dictionaryRepository.loadDictionaryById(idDictionary)
        }
    }

    fun loadCurrentSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            loadingMode.value =
                settingsRepository.getModeSettingsById(dictionary!!.idMode)?.toMode()
        }
    }

    private suspend fun deleteUnfulfilledNotifications(words: List<Word>?) {
        val currentDate = Date().time
        val prevMode = loadingMode.value
        prevMode?.let {
            words?.forEach { word ->
                val history =
                    dictionaryRepository.loadHistoryNotification(word.idWord, prevMode.idMode)
                history?.forEach {
                    if (it.dateMention > currentDate) {
                        dictionaryRepository.deleteNotificationHistoryItem(it)
                    }
                }
            }
        }
    }

    fun saveNewSettings(settings: ModeSettingsDto, resetSteps: Boolean) {
        val words = dictionary?.wordList?.map { it.copy() }
        into(liveResult, scope = MainScope()) {
            deleteUnfulfilledNotifications(words)
            dictionaryRepository.updateIncludeDictionary(true, idDictionary)
            val idMode = settingsRepository.saveModeSettings(settings)
            settings.idMode = idMode
            if (resetSteps) {
                resettingAlgorithm(words, settings)
            }
        }
    }

    private suspend fun resettingAlgorithm(words: List<Word>?, mode: ModeSettingsDto) {
        words?.forEach { word ->
            val history = dictionaryRepository.loadHistoryNotification(word.idWord, mode.idMode)
            val steps = history?.size ?: 0
            val all = (mode.selectedMode?.getCountSteps() ?: Integer.MAX_VALUE) <= steps
            val lastDate = history?.maxByOrNull { it.dateMention }?.dateMention ?: 0L
            word.learnStep = steps
            word.allNotificationsCreated = all
            word.lastDateMention = lastDate
            dictionaryRepository.updateWord(word)
        }
    }
}