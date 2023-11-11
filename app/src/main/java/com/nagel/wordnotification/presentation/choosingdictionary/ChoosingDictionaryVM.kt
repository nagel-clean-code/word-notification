package com.nagel.wordnotification.presentation.choosingdictionary

import androidx.lifecycle.viewModelScope
import com.nagel.wordnotification.R
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.dictionaries.room.DictionaryDao
import com.nagel.wordnotification.data.settings.SettingsRepository
import com.nagel.wordnotification.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
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

    fun addDictionary(name: String, idAccount: Long) {
        if (name.isBlank()) {
            return
        }
        dictionaryRepository.loadDictionaryByName(name, idAccount) { dicrionary ->
            if (dicrionary != null) {
                showMessage.value = R.string.such_dictionary_already_exists
            } else {
                dictionaryRepository.createDictionary(name, idAccount) {
                    showMessage.value = R.string.dictionary_has_been_created_successfully
                }
            }
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