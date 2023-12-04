package com.nagel.wordnotification.presentation.addingwords.worddetails

import androidx.lifecycle.MutableLiveData
import com.nagel.wordnotification.data.settings.SettingsRepository
import com.nagel.wordnotification.data.settings.entities.ModeSettingsDto
import com.nagel.wordnotification.presentation.base.BaseViewModel
import com.nagel.wordnotification.presentation.base.MutableLiveResult
import com.nagel.wordnotification.presentation.navigator.NavigatorV2
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class WordDetailsVM @Inject constructor(
    private val settingsRepository: SettingsRepository,
    val navigator: NavigatorV2
) : BaseViewModel() {

    val liveResult: MutableLiveResult<ModeSettingsDto?> = MutableLiveData()


    fun loadMode(idDictionary: Long) {
        into(liveResult, Dispatchers.IO) {
            val mode = settingsRepository.getModeSettings(idDictionary)
            return@into mode?.toMode()
        }
    }

}