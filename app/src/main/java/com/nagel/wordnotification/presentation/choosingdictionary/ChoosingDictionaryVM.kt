package com.nagel.wordnotification.presentation.choosingdictionary

import androidx.lifecycle.viewModelScope
import com.nagel.wordnotification.R
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.dictionaries.room.DictionaryDao
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.data.settings.SettingsRepository
import com.nagel.wordnotification.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChoosingDictionaryVM @Inject constructor(
    val dictionaryRepository: DictionaryRepository,
    val settingsRepository: SettingsRepository,
    private val dictionaryDao: DictionaryDao
) : BaseViewModel() {

    val showMessage = MutableStateFlow<Int?>(null)
    val loadingWords = MutableStateFlow<List<Word>?>(null)
    var idAccount = -1L
    val dictionaries: Flow<List<Dictionary>> by lazy {
        dictionaryRepository.loadDictionariesFlow(idAccount)
    }

    init {
        viewModelScope.launch {
            loadingWords.value = dictionaryDao.getAllWords().map { it.toWord() }
        }
    }

    fun toggleActiveDictionary(dictionary: Long, active: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dictionaryRepository.updateIncludeDictionary(active, dictionary)
        }
    }

    fun replaceNameDictionary(name: String, idDictionary: Long) {
        viewModelScope.launch {
            if (dictionaries.last().map { it.name }.contains(name)) {
                showMessage.value = R.string.such_dictionary_already_exists
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
                showMessage.emit(R.string.such_dictionary_already_exists)
            } else {
                createDictionary(name, idAccount)
            }
        }
    }

    private fun createDictionary(name: String, idAccount: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dictionaryRepository.createDictionary(name, idAccount)
            showMessage.tryEmit(R.string.dictionary_has_been_created_successfully)
        }
    }

    fun deleteWord(idWord: Long, success: () -> Unit) {
        dictionaryRepository.deleteDictionaryById(idWord) { successfully ->
            if (successfully) {
                success.invoke()
            } else {
                showMessage.value = R.string.couldnt_delete_dictionary
            }
        }
    }
}