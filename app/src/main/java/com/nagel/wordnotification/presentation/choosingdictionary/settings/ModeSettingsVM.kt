package com.nagel.wordnotification.presentation.choosingdictionary.settings

import com.nagel.wordnotification.data.settings.SettingsRepository
import com.nagel.wordnotification.data.settings.entities.ModeSettingsDto
import com.nagel.wordnotification.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ModeSettingsVM @Inject constructor(
    private val dictionaryRepository: SettingsRepository
) : BaseViewModel() {

    var idDictionary: Long = -1
    fun saveSettings(settings: ModeSettingsDto) {
        dictionaryRepository.saveModeSettings(settings)
    }

}