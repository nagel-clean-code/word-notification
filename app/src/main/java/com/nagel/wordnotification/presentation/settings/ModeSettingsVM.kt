package com.nagel.wordnotification.presentation.settings

import androidx.lifecycle.viewModelScope
import com.nagel.wordnotification.data.settings.SettingsRepository
import com.nagel.wordnotification.data.settings.entities.ModeSettingsDto
import com.nagel.wordnotification.data.settings.entities.SelectedMode
import com.nagel.wordnotification.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ModeSettingsVM @Inject constructor(
    private val dictionaryRepository: SettingsRepository
) : BaseViewModel() {

    var idDictionary: Long = -1
    var selectedMode: SelectedMode? = SelectedMode.PlateauEffect
    val loadingMode = MutableStateFlow<ModeSettingsDto?>(null)


    fun loadCurrentSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            loadingMode.value = dictionaryRepository.getModeSettings(idDictionary)?.toMode()
        }
    }

    fun saveSettings(settings: ModeSettingsDto) {
        viewModelScope.launch(Dispatchers.IO) {
            dictionaryRepository.saveModeSettings(settings)
        }
    }

}