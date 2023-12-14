package com.nagel.wordnotification.presentation.addingwords.worddetails

import androidx.lifecycle.MutableLiveData
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.NotificationHistoryItem
import com.nagel.wordnotification.data.settings.SettingsRepository
import com.nagel.wordnotification.data.settings.entities.ModeSettingsDto
import com.nagel.wordnotification.presentation.base.BaseViewModel
import com.nagel.wordnotification.presentation.base.MutableLiveResult
import com.nagel.wordnotification.presentation.navigator.NavigatorV2
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class WordDetailsVM @Inject constructor(
    private val settingsRepository: SettingsRepository,
    var dictionaryRepository: DictionaryRepository,
    val navigator: NavigatorV2
) : BaseViewModel() {

    val loadModeStatus: MutableLiveResult<ModeSettingsDto?> = MutableLiveData()

    fun loadMode(idMode: Long) {
        into(loadModeStatus) {
            val mode = settingsRepository.getModeSettingsById(idMode)
            return@into mode?.toMode()
        }
    }

    fun loadNotificationHistory(idWord: Long, idMode: Long): Flow<List<NotificationHistoryItem>?> {
        return dictionaryRepository.loadHistoryNotificationFlow(idWord, idMode)
    }
}