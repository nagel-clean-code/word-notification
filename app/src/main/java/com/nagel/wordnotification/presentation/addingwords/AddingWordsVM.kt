package com.nagel.wordnotification.presentation.addingwords

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.nagel.wordnotification.R
import com.nagel.wordnotification.core.algorithms.AlgorithmHelper
import com.nagel.wordnotification.core.services.NotificationDto
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.dictionaries.entities.NotificationHistoryItem
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.settings.SettingsRepository
import com.nagel.wordnotification.presentation.base.BaseViewModel
import com.nagel.wordnotification.presentation.navigator.NavigatorV2
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AddingWordsVM @Inject constructor(
    val dictionaryRepository: DictionaryRepository,
    private val settingsRepository: SettingsRepository,
    @ApplicationContext val context: Context,
    val navigator: NavigatorV2
) : BaseViewModel() {
    private val defaultNameDictionary =
        context.resources.getString(R.string.default_name_dictionary)
    val loadedDictionaryFlow = MutableStateFlow(false)
    val showMessage = MutableStateFlow<String?>(null)

    var currentDictionaryName = defaultNameDictionary
    private val coroutineExceptionHandler = CoroutineExceptionHandler() { _, ex ->
        ex.printStackTrace()
    }

    var dictionary: Dictionary? = null

    fun loadDictionaryByName(name: String = currentDictionaryName, idAccount: Long) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            dictionaryRepository.loadDictionaryByName(name, idAccount) {
                if (it == null) {
                    Log.d(TAG, "Словаря нет")
                    createDictionary(idAccount)
                } else {
                    Log.d(TAG, "Словарь загружен")
                    dictionary = it
                    loadedDictionaryFlow.value = true
                }
            }
        }
    }

    fun loadDictionaryById(idDictionary: Long) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            dictionaryRepository.loadDictionaryById(idDictionary) {
                dictionary = it
                loadedDictionaryFlow.value = true
            }
        }
    }

    fun repeatNotification(word: Word) {
        viewModelScope.launch(Dispatchers.IO) {
            val mode = settingsRepository.getModeSettings(word.idDictionary)
            mode?.let {
                dictionaryRepository.loadHistoryNotification(word.idWord, mode.idMode)
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
                showMessage.value = navigator.getString(R.string.couldnt_delete_word)
            }
        }
    }

    private fun createDictionary(idAccount: Long) {
        Log.d(TAG, "Создаю словарь")
        viewModelScope.launch(Dispatchers.IO) {
            val result = dictionaryRepository.createDictionary(defaultNameDictionary, idAccount)
            withContext(Dispatchers.Main) {
                dictionary = result
                loadedDictionaryFlow.value = true
                Log.d(TAG, "Словарь создан")
            }
        }
    }

    companion object {
        private const val TAG = "ADDING_WORDS_VIEW_MODEL"
    }
}