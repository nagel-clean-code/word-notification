package com.nagel.wordnotification.presentation.settings

import androidx.lifecycle.viewModelScope
import com.nagel.wordnotification.core.algorithms.Algorithm
import com.nagel.wordnotification.core.algorithms.PlateauEffect
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.settings.SettingsRepository
import com.nagel.wordnotification.data.settings.entities.ModeSettingsDto
import com.nagel.wordnotification.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ModeSettingsVM @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val dictionaryRepository: DictionaryRepository,
) : BaseViewModel() {

    var idDictionary: Long = -1
    var selectedMode: Algorithm? = PlateauEffect
    val loadingMode = MutableStateFlow<ModeSettingsDto?>(null)
    var words: List<Word>? = null


    fun resettingAlgorithm(word: Word){
        viewModelScope.launch(Dispatchers.IO) {
            word.learnStep = 0
            word.allNotificationsCreated = false
            word.lastDateMention = 0
            dictionaryRepository.updateWord(word)
        }
    }

    fun loadWords(idDictionary: Long){
        viewModelScope.launch(Dispatchers.IO) {
            words = dictionaryRepository.getWordsByIdDictionary(idDictionary)
        }
    }

    fun loadCurrentSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            loadingMode.value = settingsRepository.getModeSettings(idDictionary)?.toMode()
        }
    }

    fun saveSettings(settings: ModeSettingsDto) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.saveModeSettings(settings)
        }
    }

}