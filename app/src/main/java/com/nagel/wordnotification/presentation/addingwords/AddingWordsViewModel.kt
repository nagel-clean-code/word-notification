package com.nagel.wordnotification.presentation.addingwords

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.nagel.wordnotification.R
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddingWordsViewModel @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
    @ApplicationContext context: Context
) : BaseViewModel() {
    private val defaultNameDictionary =
        context.resources.getString(R.string.default_name_dictionary)
    val loadedDictionaryFlow = MutableStateFlow(false)

    fun loadDictionary(name: String = defaultNameDictionary, idAccount: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dictionaryRepository.loadDictionary(name, idAccount){
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