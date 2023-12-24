package com.nagel.wordnotification.presentation.addingwords

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.nagel.wordnotification.R
import com.nagel.wordnotification.core.algorithms.AlgorithmHelper
import com.nagel.wordnotification.core.services.NotificationDto
import com.nagel.wordnotification.data.accounts.entities.Account
import com.nagel.wordnotification.data.accounts.room.AccountDao
import com.nagel.wordnotification.data.accounts.room.entities.AccountDbEntity
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.dictionaries.entities.NotificationHistoryItem
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.data.settings.SettingsRepository
import com.nagel.wordnotification.presentation.base.BaseViewModel
import com.nagel.wordnotification.presentation.navigator.NavigatorV2
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.bush.translator.Language
import me.bush.translator.Translator
import me.bush.translator.languageOf
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AddingWordsVM @Inject constructor(
    val dictionaryRepository: DictionaryRepository,
    private val settingsRepository: SettingsRepository,
    val navigator: NavigatorV2,
    private val accountDao: AccountDao,
    private var sessionRepository: SessionRepository
) : BaseViewModel() {
    private val coroutineExceptionHandler = CoroutineExceptionHandler() { _, ex ->
        ex.printStackTrace()
    }
    private val defaultNameDictionary = navigator.getString(R.string.default_name_dictionary)

    private val _loadedDictionaryFlow = MutableStateFlow<Dictionary?>(null)
    val loadedDictionaryFlow: StateFlow<Dictionary?> = _loadedDictionaryFlow
    private var permissionShowPreview = sessionRepository.getPreviewFlag(SCREE_CODE)
    private var isAutoTranslation = sessionRepository.getAutoTranslation()
    private var jobTranslation: Job? = null
    private val translator = Translator()
    private lateinit var language: Language
    val showTranslate = MutableStateFlow<String?>(null)


    init {
        setTranslateLang(sessionRepository.getTranslationLanguage())
    }


    fun requestTranslation(text: String) {
        if (!isAutoTranslation) return
        jobTranslation?.cancel()
        jobTranslation = viewModelScope.launch(coroutineExceptionHandler) {
            delay(500)
            val translation = translator.translate(text, language, Language.AUTO)
            if (translation.translatedText.isNotBlank()) {
                showTranslate.emit(translation.translatedText)
            }
        }
    }

    fun setTranslateLang(lang: String) {
        language = languageOf(lang) ?: Language.ENGLISH
    }

    fun setAutoTranslation(isAuto: Boolean) {
        isAutoTranslation = isAuto
    }

    fun getPermissionShowPreview(): Boolean {
        return if (permissionShowPreview) {
            permissionShowPreview = false
            true
        } else {
            false
        }
    }

    fun start() {
        viewModelScope.launch {
            startSession()
            loadCurrentDictionary()
        }
    }

    private suspend fun startSession() {
        withContext(Dispatchers.IO) {
            val currentSession = sessionRepository.getSession()
            if (currentSession.account == null) {
                createNewUser()
            }
        }
    }

    private suspend fun loadCurrentDictionary() {
        val idDictionary = sessionRepository.getSession().currentDictionaryId
        if (idDictionary != -1L && idDictionary != null) {
            loadDictionaryById(idDictionary)
        } else {
            getFirstOrCreateDictionary()
        }
    }

    private suspend fun getFirstOrCreateDictionary() {
        val dictionary = getFirstDictionary(getAccountId())
        if (dictionary != null) {
            sessionRepository.saveCurrentIdDictionary(dictionary.idDictionary)
            _loadedDictionaryFlow.emit(dictionary)
        } else {
            createBeginDictionary()
        }
    }

    private suspend fun createBeginDictionary() {
        Log.d(TAG, "Создаю словарь")
        withContext(Dispatchers.IO + coroutineExceptionHandler) {
            val accountId = sessionRepository.getAccountId()!!
            val dictionary = dictionaryRepository.createDictionary(defaultNameDictionary, accountId)
            _loadedDictionaryFlow.emit(dictionary)
            Log.d(TAG, "Словарь создан")
        }
    }

    private suspend fun createNewUser() {
        val account = createAndSaveAccountInDb()
        sessionRepository.saveAccount(account)
    }

    private suspend fun createAndSaveAccountInDb(): Account {
        val accountDbEntity = AccountDbEntity.createAccount()
        val id = accountDao.addAccount(accountDbEntity)
        val account = accountDbEntity.toAccount()
        account.id = id
        return account
    }

    private suspend fun loadDictionaryById(idDictionary: Long) {
        withContext(Dispatchers.IO + coroutineExceptionHandler) {
            val result = dictionaryRepository.loadDictionaryById(idDictionary)
            if (result != null) {
                _loadedDictionaryFlow.emit(result)
            } else {
                getFirstOrCreateDictionary()
            }
        }
    }

    fun loadWords(): Flow<List<Word>> {
        val id = _loadedDictionaryFlow.value!!.idDictionary
        return dictionaryRepository.loadWordsByIdDictionaryFlow(id)
    }

    fun repeatNotification(word: Word) {
        viewModelScope.launch(Dispatchers.IO) {
            val mode = settingsRepository.getModeSettingsById(_loadedDictionaryFlow.value!!.idMode)
            mode?.let {
                dictionaryRepository.loadHistoryNotificationFlow(word.idWord, mode.idMode)
                    .collect() { list ->
                        withContext(Dispatchers.Main) {
                            createOldNotification(list, word)
                        }
                        coroutineContext.job.cancel()
                    }
            }
        }
    }

    private fun createOldNotification(list: List<NotificationHistoryItem>?, word: Word) {
        val currentDate = Date().time
        list?.filter { it.dateMention > currentDate }?.forEach() {
            val notification = NotificationDto(
                word.textFirst,
                word.textLast,
                it.dateMention,
                word.uniqueId,
                it.learnStep
            )
            AlgorithmHelper.createAlarm(notification)
        }
    }

    fun deleteWord(idWord: Long, success: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch() {
            val count = dictionaryRepository.deleteWordById(idWord)
            if (count > 0) {
                withContext(Dispatchers.Main) {
                    success.invoke()
                }
            } else {
                navigator.toast(R.string.couldnt_delete_word)
            }
        }
    }

    private suspend fun getFirstDictionary(idAccount: Long?): Dictionary? {
        if (idAccount == null) return null
        val dictionaries = dictionaryRepository.loadDictionaries(idAccount)
        return if (dictionaries.isEmpty()) {
            null
        } else {
            dictionaries.first()
        }
    }

    private fun getAccountId() = sessionRepository.getSession().account?.id

    companion object {
        private const val TAG = "ADDING_WORDS_VIEW_MODEL"
        const val SCREE_CODE = "AddingWordsFragment"
    }
}