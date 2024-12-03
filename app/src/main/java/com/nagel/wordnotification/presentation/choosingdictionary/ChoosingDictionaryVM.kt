package com.nagel.wordnotification.presentation.choosingdictionary

import androidx.lifecycle.viewModelScope
import com.nagel.wordnotification.R
import com.nagel.wordnotification.core.algorithms.NotificationAlgorithm
import com.nagel.wordnotification.core.services.Utils
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.data.settings.SettingsRepository
import com.nagel.wordnotification.presentation.base.BaseViewModel
import com.nagel.wordnotification.presentation.navigator.NavigatorV2
import com.nagel.wordnotification.presentation.exportAndImport.ExportGenerator
import com.nagel.wordnotification.utils.GlobalFunction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class ChoosingDictionaryVM @Inject constructor(
    val dictionaryRepository: DictionaryRepository,
    val settingsRepository: SettingsRepository,
    private var navigatorV2: NavigatorV2,
    private val notificationAlgorithm: NotificationAlgorithm,
    private var sessionRepository: SessionRepository,
    private val exportGenerator: ExportGenerator
) : BaseViewModel() {

    var idAccount = -1L
    val dictionaries: Flow<List<Dictionary>> by lazy {
        dictionaryRepository.loadDictionariesFlow(idAccount)
    }
    var listDictionary: List<Dictionary>? = null
    var isStarted = AtomicBoolean(false)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            isStarted.set(sessionRepository.getIsStarted())
        }
    }

    fun toggleActiveDictionary(
        active: Boolean,
        dictionary: Dictionary
    ) {
        viewModelScope.launch {
            dictionaryRepository.updateIncludeDictionary(active, dictionary.idDictionary)
            if (active == false) {
                val idWord = sessionRepository.getCurrentWordIdNotification()
                dictionary.wordList.forEach { word ->
                    if (word.idWord == idWord) {
                        Utils.deleteNotification(word)
                        word.uniqueId = GlobalFunction.generateUniqueId()
                        dictionaryRepository.updateWord(word)
                    }
                }
            }
            notificationAlgorithm.createNotification()
        }
    }

    fun replaceNameDictionary(name: String, idDictionary: Long) {
        viewModelScope.launch {
            if (listDictionary?.map { it.name }?.contains(name) == true) {
                navigatorV2.toast(R.string.such_dictionary_already_exists)
                return@launch
            }
            if (name.isBlank()) {
                return@launch
            }
            viewModelScope.launch(Dispatchers.IO) {
                dictionaryRepository.updateNameDictionary(idDictionary, name)
            }
        }
    }

    fun addDictionary(name: String, idAccount: Long) {
        if (name.isBlank()) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val dictionary = dictionaryRepository.loadDictionaryByName(name, idAccount)
            if (dictionary != null) {
                withContext(Dispatchers.Main) {
                    navigatorV2.toast(R.string.such_dictionary_already_exists)
                }
            } else {
                createDictionary(name, idAccount)
            }
        }
    }

    private fun createDictionary(name: String, idAccount: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dictionaryRepository.createDictionary(name, idAccount)
            withContext(Dispatchers.Main) {
                navigatorV2.toast(R.string.dictionary_has_been_created_successfully)
            }
        }
    }

    fun deleteDictionary(dictionary: Dictionary, success: () -> Unit) {
        dictionaryRepository.deleteDictionaryById(dictionary.idDictionary) { successfully ->
            if (successfully) {
                val idWord = sessionRepository.getCurrentWordIdNotification()
                dictionary.wordList.forEach {
                    if (it.idWord == idWord) {
                        viewModelScope.launch {
                            notificationAlgorithm.createNotification()
                        }
                    }
                }
                success.invoke()
            } else {
                navigatorV2.toast(R.string.couldnt_delete_dictionary)
            }
        }
    }

    fun copyDictionary(dictionary: Dictionary, success: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val name = dictionary.name + " copy"
            val newDic = dictionaryRepository.createDictionary(name, idAccount, false)
            dictionary.wordList.forEach {
                dictionaryRepository.addWord(
                    it.copy(
                        idDictionary = newDic.idDictionary,
                        allNotificationsCreated = false,
                        learnStep = 0,
                        lastDateMention = 0,
                        uniqueId = GlobalFunction.generateUniqueId()
                    )
                )
            }
            delay(300)
            withContext(Dispatchers.Main) {
                success.invoke()
            }
        }
    }

    fun exportDictionary(dictionary: Dictionary, sendFile: (File) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = exportGenerator.writeOneDictionary(dictionary)
            withContext(Dispatchers.Main) {
                sendFile(file)
            }
        }
    }

    override fun onCleared() {
        exportGenerator.deleteLastFile()
        super.onCleared()
    }
}