package com.nagel.wordnotification.presentation.choosingdictionary

import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class ChoosingDictionaryVM @Inject constructor(
    val dictionaryRepository: DictionaryRepository
) : BaseViewModel() {

    val showMessage = MutableStateFlow<String?>(null)

    fun addDictionary(name: String, idAccount: Long) {
        dictionaryRepository.loadDictionaryByName(name, idAccount) { found ->
            if (found) {
                showMessage.value = "Такой словарь уже существует"
            } else {
                dictionaryRepository.createDictionary(name, idAccount) {
                    showMessage.value = "Словарь успешно создан"
                }
            }
        }
    }
}