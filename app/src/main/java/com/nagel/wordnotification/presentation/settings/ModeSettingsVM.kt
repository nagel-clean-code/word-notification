package com.nagel.wordnotification.presentation.settings

import androidx.lifecycle.viewModelScope
import com.nagel.wordnotification.R
import com.nagel.wordnotification.core.algorithms.Algorithm
import com.nagel.wordnotification.core.algorithms.NotificationAlgorithm
import com.nagel.wordnotification.core.algorithms.PlateauEffect
import com.nagel.wordnotification.core.services.Utils
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.data.settings.SettingsRepository
import com.nagel.wordnotification.data.settings.entities.ModeSettingsDto
import com.nagel.wordnotification.presentation.base.BaseViewModel
import com.nagel.wordnotification.presentation.navigator.NavigatorV2
import com.nagel.wordnotification.utils.GlobalFunction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class ModeSettingsVM @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val dictionaryRepository: DictionaryRepository,
    private val notificationAlgorithm: NotificationAlgorithm,
    private val navigator: NavigatorV2,
    private var sessionRepository: SessionRepository
) : BaseViewModel() {

    var idDictionary: Long = -1
    var selectedMode: Algorithm? = PlateauEffect
    val loadingMode = MutableStateFlow<ModeSettingsDto?>(null)
    var dictionary: Dictionary? = null

    var isStarted = AtomicBoolean(false)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            isStarted.set(sessionRepository.getIsStarted())
        }
    }

    fun preload(idDictionary: Long) {
        this.idDictionary = idDictionary
        viewModelScope.launch(Dispatchers.IO) {
            dictionary = dictionaryRepository.loadDictionaryById(idDictionary)
            loadingMode.value =
                settingsRepository.getModeSettingsById(dictionary!!.idMode)?.toMode()
        }
    }

    suspend fun resetStepsSetTimeToCurrentOne(wordList: List<Word>) {
        val currentTime = Date().time
        val newList = wordList.map {
            it.fullCopyWord(
                learnStep = 0,
                lastDateMention = currentTime,
                allNotificationsCreated = false
            )
        }
        newList.forEach {
            dictionaryRepository.updateWord(it)
        }
    }

    fun resettingAlgorithm() {
        val currentTime = Date().time
        val idWord = sessionRepository.getCurrentWordIdNotification()
        viewModelScope.launch(Dispatchers.IO) {
            dictionary?.apply {
                resetHistory(wordList)
                wordList.forEach { word ->
                    val newWord = word.apply {
                        if (this.idWord == idWord) {
                            Utils.deleteNotification(this)
                            uniqueId = GlobalFunction.generateUniqueId()
                        }
                        lastDateMention = currentTime
                        learnStep = 0
                        allNotificationsCreated = false
                    }
                    dictionaryRepository.updateWord(newWord)
                }
                withContext(Dispatchers.Main) {
                    notificationAlgorithm.createNotification()
                    navigator.toast(R.string.algorithm_has_been_reset)
                }
            }
        }
    }

    suspend fun resetHistory(wordList: List<Word>) {
        wordList.forEach {
            dictionaryRepository.deleteNotificationsHistoryByIdWord(it.idWord)
        }
    }

    fun getStatusNotificationDictionary(): Boolean = dictionary?.include == true

    fun saveNewSettings(settings: ModeSettingsDto, success: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            dictionaryRepository.updateIncludeDictionary(true, idDictionary)
            dictionary?.include = true
            val idMode = settingsRepository.saveModeSettings(settings)
            settings.idMode = idMode
            withContext(Dispatchers.Main) {
                success.invoke()
            }
        }
    }

    suspend fun tryReinstallNotification(
        newMode: ModeSettingsDto,
        prevMode: ModeSettingsDto?,
        success: () -> Unit
    ) {
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
            notificationAlgorithm.createNotification()
        }
        success.invoke()
    }
}