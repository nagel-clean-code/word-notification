package com.nagel.wordnotification.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagel.wordnotification.R
import com.nagel.wordnotification.core.algorithms.NotificationAlgorithm
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.premium.PremiumRepository
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.domain.googledisk.files.FilesRepository
import com.nagel.wordnotification.presentation.exportAndImport.ExportGenerator
import com.nagel.wordnotification.presentation.navigator.NavigatorV2
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class MainActivityVM @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val premiumRepository: PremiumRepository,
    private val notificationAlgorithm: NotificationAlgorithm,
    private val exportGenerator: ExportGenerator,
    private val dictionaryRepository: DictionaryRepository,
    private val filesRepository: FilesRepository,
    private val navigator: NavigatorV2,
) : ViewModel() {

    init {
        viewModelScope.launch(Dispatchers.IO) {
            if (sessionRepository.getIsAutoBackupAndMark() && premiumRepository.getIsStarted()) {
                uploadBackupToGoogleDisk()
            }
        }
    }

    private suspend fun uploadBackupToGoogleDisk() {
        val file = generateBackupFile(false)
        file?.let {
            filesRepository.upload(file)
        }
        navigator.toast(R.string.backup_has_been_performed)
    }

    private suspend fun generateBackupFile(fileNameWithDate: Boolean): File? {
        val accountId = sessionRepository.getAccountId() ?: return null
        return withContext(Dispatchers.Default) {
            val list = dictionaryRepository.loadDictionaries(accountId)
            return@withContext exportGenerator.writeDictionaries(
                list, true, fileNameWithDate
            )
        }
    }

    fun isItPossibleShowRateApp(): Boolean {
        val session = sessionRepository.getSession()
        val step = session.stepRatedApp ?: return false
        val date = session.dateAppInstallation ?: return false
        val nextInterval = mapSteps[step] ?: return false
        return Date().time - date > nextInterval
    }

    fun startNotification() {
        viewModelScope.launch {
            notificationAlgorithm.createNotification()
        }
    }

    fun saveIsStarted(isStarted: Boolean) {
        premiumRepository.saveIsStarted(isStarted)
    }

    companion object {
        private const val BEGINNING_OF_SHOW = 3 * 24 * 60 * 60 * 1000L

        private val mapSteps = mapOf(
            0 to BEGINNING_OF_SHOW,             //3 дня
            1 to BEGINNING_OF_SHOW * 4,         //12 дней
            2 to BEGINNING_OF_SHOW * 10,        //1 месяца
            3 to BEGINNING_OF_SHOW * 10 * 2,    //2 месяца
            4 to BEGINNING_OF_SHOW * 10 * 4,    //4 месяца
            5 to BEGINNING_OF_SHOW * 10 * 12,   //1 год
        )
    }
}