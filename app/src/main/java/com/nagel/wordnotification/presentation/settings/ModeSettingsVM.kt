package com.nagel.wordnotification.presentation.settings

import androidx.lifecycle.viewModelScope
import com.nagel.wordnotification.core.algorithms.Algorithm
import com.nagel.wordnotification.core.algorithms.NotificationAlgorithm
import com.nagel.wordnotification.core.algorithms.PlateauEffect
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.data.settings.SettingsRepository
import com.nagel.wordnotification.data.settings.entities.ModeSettingsDto
import com.nagel.wordnotification.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ModeSettingsVM @Inject constructor(
    private var sessionRepository: SessionRepository,
    private val settingsRepository: SettingsRepository,
    private val dictionaryRepository: DictionaryRepository,
    private val notificationAlgorithm: NotificationAlgorithm
) : BaseViewModel() {

    var idDictionary: Long = -1
    var selectedMode: Algorithm? = PlateauEffect
    val loadingMode = MutableStateFlow<ModeSettingsDto?>(null)
    var dictionary: Dictionary? = null

    fun preload(idDictionary: Long) {
        this.idDictionary = idDictionary
        viewModelScope.launch(Dispatchers.IO) {
            dictionary = dictionaryRepository.loadDictionaryById(idDictionary)
            loadingMode.value =
                settingsRepository.getModeSettingsById(dictionary!!.idMode)?.toMode()
        }
    }

    fun resetStepsSetTimeToCurrentOne(wordList: List<Word>) {
        val currentTime = Date().time
        val newList = wordList.map {
            it.copy(
                learnStep = 0,
                lastDateMention = currentTime,
                allNotificationsCreated = false
            )
        }
        viewModelScope.launch {
            newList.forEach {
                dictionaryRepository.updateWord(it)
            }
        }
    }

    fun resetHistory(wordList: List<Word>) {
        viewModelScope.launch {
            wordList.forEach {
                dictionaryRepository.deleteNotificationsHistoryByIdWord(it.idWord)
            }
        }
    }

    fun getStatusNotificationDictionary(): Boolean = dictionary?.include == true

    fun saveNewSettings(settings: ModeSettingsDto, success: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            dictionaryRepository.updateIncludeDictionary(true, idDictionary)
            val idMode = settingsRepository.saveModeSettings(settings)
            settings.idMode = idMode
            withContext(Dispatchers.Main) {
                success.invoke()
            }
        }
    }

    fun tryReinstallNotification(
        newMode: ModeSettingsDto,
        prevMode: ModeSettingsDto?,
        success: () -> Unit
    ) {
        viewModelScope.launch {
            val needReinstallNotification = newMode.selectedMode != prevMode?.selectedMode ||
                    newMode.sampleDays != prevMode?.sampleDays ||
                    newMode.timeIntervals != prevMode.timeIntervals

            val changeTime: Boolean =
                if (newMode.timeIntervals == prevMode?.timeIntervals && newMode.timeIntervals) {
                    newMode.workingTimeInterval.first != prevMode.workingTimeInterval.first ||
                            newMode.workingTimeInterval.second != prevMode.workingTimeInterval.second
                } else {
                    false
                }

            if (dictionary?.include == true && (needReinstallNotification || changeTime)) {
                val idCurrentWordNotification = sessionRepository.getCurrentWordIdNotification()
                val containsWordInDictionary = dictionary?.wordList?.map {
                    it.idWord
                }?.contains(idCurrentWordNotification)
                if (containsWordInDictionary == true) {
                    val word = dictionaryRepository.getWordById(idCurrentWordNotification)
                    word?.apply {
                        mode = settingsRepository.getModeSettingsById(newMode.idMode)
                    }
                    notificationAlgorithm.createNotification(word)
                }
            }
            success.invoke()
        }
    }
}