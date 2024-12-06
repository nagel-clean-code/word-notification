package com.nagel.wordnotification.presentation.addingwords

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.nagel.wordnotification.Constants.NUMBER_OF_FREE_WORDS_PER_ADVERTISEMENT
import com.nagel.wordnotification.R
import com.nagel.wordnotification.core.algorithms.NotificationAlgorithm
import com.nagel.wordnotification.core.services.Utils
import com.nagel.wordnotification.data.accounts.entities.Account
import com.nagel.wordnotification.data.accounts.room.AccountDao
import com.nagel.wordnotification.data.accounts.room.entities.AccountDbEntity
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.premium.PremiumRepository
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.presentation.base.BaseViewModel
import com.nagel.wordnotification.presentation.navigator.NavigatorV2
import com.nagel.wordnotification.utils.GlobalFunction
import dagger.hilt.android.lifecycle.HiltViewModel
import io.appmetrica.analytics.AppMetrica
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.bush.translator.Language
import me.bush.translator.Translator
import me.bush.translator.languageOf
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

enum class TranslationWord {
    FIRST_WORD, LAST_WORD
}

@HiltViewModel
class AddingWordsVM @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
    val navigator: NavigatorV2,
    private val accountDao: AccountDao,
    private var sessionRepository: SessionRepository,
    private val notificationAlgorithm: NotificationAlgorithm,
    private var premiumRepository: PremiumRepository
) : BaseViewModel() {

    private val coroutineExceptionHandler = CoroutineExceptionHandler() { _, ex ->
        ex.printStackTrace()
    }
    private val defaultNameDictionary = navigator.getString(R.string.default_name_dictionary)
    val addNumberFreeWords = AtomicInteger(NUMBER_OF_FREE_WORDS_PER_ADVERTISEMENT)

    private val _loadedDictionaryFlow = MutableStateFlow<Dictionary?>(null)
    val loadedDictionaryFlow: StateFlow<Dictionary?> = _loadedDictionaryFlow
    private var permissionShowPreview = sessionRepository.getPreviewFlag(SCREE_CODE)
    private var isAutoTranslation = sessionRepository.getAutoTranslation()
    private var jobTranslation: Job? = null
    private val translator = Translator()

    var currentAutoTranslate: TranslationWord = TranslationWord.LAST_WORD
    private var currentLanguage: Language = Language.ENGLISH
    private var languageLocale: Language =
        languageOf(Locale.getDefault().language) ?: Language.RUSSIAN

    val showTranslateLastWord = MutableStateFlow<String?>(null)
    val showTranslateFirstWord = MutableStateFlow<String?>(null)

    var isStarted = AtomicBoolean(false)
    private var countAllWords = -1


    init {
        viewModelScope.launch(Dispatchers.Default) {
            setTranslateLang(sessionRepository.getTranslationLanguage())
            addNumberFreeWords.set(premiumRepository.getAddNumberFreeWords())
        }
        getDictionaries()
        viewModelScope.launch(Dispatchers.IO) {
            isStarted.set(premiumRepository.getIsStarted())
        }
    }

    private fun getDictionaries() {
        viewModelScope.launch(Dispatchers.IO) {
            dictionaryRepository.getAllWordsFlow().collect() {
                withContext(Dispatchers.Main) {
                    countAllWords = it.size
                }
            }
        }
    }

    fun accessibilityOfAddOn(): Boolean {
        if (isStarted.get()) return true
        return countAllWords < premiumRepository.getCurrentLimitWord()
    }

    fun addFreeWords() {
        val count = premiumRepository.getCurrentLimitWord()
        premiumRepository.saveCurrentLimitWords(count + NUMBER_OF_FREE_WORDS_PER_ADVERTISEMENT)
    }

    fun changeCurrentAutoTranslate() {
        currentAutoTranslate = if (currentAutoTranslate == TranslationWord.LAST_WORD) {
            TranslationWord.FIRST_WORD
        } else {
            TranslationWord.LAST_WORD
        }
    }

    fun requestTranslation(text: String, typeTranslate: TranslationWord) {
        if (typeTranslate != currentAutoTranslate) return
        if (!isAutoTranslation) return
        jobTranslation?.cancel()
        jobTranslation = viewModelScope.launch(coroutineExceptionHandler) {
            if (text.isBlank()) {
                if (typeTranslate == TranslationWord.LAST_WORD) {
                    showTranslateLastWord.emit("")
                } else {
                    showTranslateFirstWord.emit("")
                }
                return@launch
            }
            delay(500)
            val lang = if (typeTranslate == TranslationWord.LAST_WORD) {
                currentLanguage
            } else {
                languageLocale
            }
            val translation = translator.translate(text, lang, Language.AUTO)
            if (typeTranslate == TranslationWord.LAST_WORD) {
                showTranslateLastWord.emit(translation.translatedText)
            } else {
                showTranslateFirstWord.emit(translation.translatedText)
            }
        }
    }

    fun getCurrentLanguageTranslate(): Language {
        return if (currentAutoTranslate == TranslationWord.LAST_WORD) {
            currentLanguage
        } else {
            languageLocale
        }
    }

    fun setTranslateLang(lang: String) {
        if (currentAutoTranslate == TranslationWord.LAST_WORD) {
            currentLanguage = languageOf(lang) ?: Language.ENGLISH
        } else {
            languageLocale = languageOf(lang) ?: Language.ENGLISH
        }
    }

    fun setAutoTranslation(isAuto: Boolean) {
        isAutoTranslation = isAuto
    }

    suspend fun addWord(word: Word): Long {
        return dictionaryRepository.addWord(word)
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
        AppMetrica.setUserProfileID(account.idAuthorUUID)
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

    fun repeatNotification() {
        viewModelScope.launch {
            updateNotification()
        }
    }

    fun deleteWord(idWord: Long) {
        CoroutineScope(Dispatchers.IO).launch() {
            val count = dictionaryRepository.deleteWordById(idWord)
            if (count > 0) {
                withContext(Dispatchers.Main) {
                    if (sessionRepository.getCurrentWordIdNotification() == idWord) {
                        notificationAlgorithm.createNotification()
                    }
                }
            } else {
                navigator.toast(R.string.couldnt_delete_word)
            }
        }
    }

    fun swapWordsInCurrentDictionary() {
        viewModelScope.launch {
            _loadedDictionaryFlow.value?.wordList?.forEach { word ->
                val first = word.textFirst
                word.textFirst = word.textLast
                word.textLast = first
                dictionaryRepository.updateText(word)
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

    fun tryCreateNotification() {
        if (loadedDictionaryFlow.value?.include == true) {
            viewModelScope.launch {
                notificationAlgorithm.createNotification()
            }
        }
    }

    private suspend fun updateNotification() {
        val idWord = sessionRepository.getCurrentWordIdNotification()
        if (idWord != -1L) {
            val word = dictionaryRepository.getWordById(idWord)
            word?.let {
                Utils.deleteNotification(word)
                word.uniqueId = GlobalFunction.generateUniqueId()
                dictionaryRepository.updateWord(word)
            }
        }
        notificationAlgorithm.createNotification()
    }

    private fun getAccountId() = sessionRepository.getSession().account?.id

    companion object {
        private const val TAG = "ADDING_WORDS_VIEW_MODEL"
        const val SCREE_CODE = "AddingWordsFragment"
    }
}