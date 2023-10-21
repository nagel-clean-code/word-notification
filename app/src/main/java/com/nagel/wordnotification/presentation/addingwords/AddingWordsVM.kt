package com.nagel.wordnotification.presentation.addingwords

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.nagel.wordnotification.R
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddingWordsVM @Inject constructor(
    val dictionaryRepository: DictionaryRepository,
    @ApplicationContext context: Context
) : BaseViewModel() {
    private val defaultNameDictionary =
        context.resources.getString(R.string.default_name_dictionary)
    val loadedDictionaryFlow = MutableStateFlow(false)
    val showMessage = MutableStateFlow<String?>(null)

    var currentDictionary = defaultNameDictionary
    private val coroutineExceptionHandler = CoroutineExceptionHandler() { _, ex ->
        ex.printStackTrace()
    }

    var loadedDictionary: Long = -1

    fun loadDictionaryByName(name: String = currentDictionary, idAccount: Long) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            dictionaryRepository.loadDictionaryByName(name, idAccount) {
                if (!it) {
                    Log.d(TAG, "Словаря нет")
                    createDictionary(idAccount)
                } else {
                    Log.d(TAG, "Словарь загружен")
                    loadedDictionaryFlow.value = true
                }
            }
        }
    }

    fun loadDictionaryById(idDictionary: Long) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            dictionaryRepository.loadDictionaryById(idDictionary) {
                loadedDictionaryFlow.value = true
            }
        }
    }

    fun deleteWord(idWord: Long, success: () -> Unit) {
        dictionaryRepository.deleteWordById(idWord) { successfully ->
            if(successfully) {
                success.invoke()
            }else{
                showMessage.value = "Не удалось удалить слово"
            }
        }
    }

    private fun createDictionary(idAccount: Long) {
        Log.d(TAG, "Создаю словарь")
        dictionaryRepository.createDictionary(defaultNameDictionary, idAccount) {
            loadedDictionaryFlow.value = true
            Log.d(TAG, "Словарь создан")
        }
    }

    companion object {
        private const val TAG = "ADDING_WORDS_VIEW_MODEL"
    }
}