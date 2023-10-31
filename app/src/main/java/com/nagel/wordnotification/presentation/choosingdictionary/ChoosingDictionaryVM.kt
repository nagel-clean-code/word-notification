package com.nagel.wordnotification.presentation.choosingdictionary

import androidx.lifecycle.viewModelScope
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.dictionaries.room.DictionaryDao
import com.nagel.wordnotification.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChoosingDictionaryVM @Inject constructor(
    val dictionaryRepository: DictionaryRepository,
    private val dictionaryDao: DictionaryDao
) : BaseViewModel() {

    val showMessage = MutableStateFlow<String?>(null)
    val loadingWords = MutableStateFlow<List<Word>?>(null)

    init {
        viewModelScope.launch {
            loadingWords.value = dictionaryDao.getAllWords().map { it.toWord() }
        }
    }

    fun addDictionary(name: String, idAccount: Long) {
        dictionaryRepository.loadDictionaryByName(name, idAccount) { dicrionary ->
            if (dicrionary != null) {
                showMessage.value = "Такой словарь уже существует"
            } else {
                dictionaryRepository.createDictionary(name, idAccount) {
                    showMessage.value = "Словарь успешно создан"
                }
            }
        }
    }
}