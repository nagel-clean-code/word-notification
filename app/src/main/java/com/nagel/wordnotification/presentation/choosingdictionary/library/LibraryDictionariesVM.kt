package com.nagel.wordnotification.presentation.choosingdictionary.library

import androidx.lifecycle.viewModelScope
import com.nagel.wordnotification.R
import com.nagel.wordnotification.core.analytecs.AppMetricaAnalytic
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.firbase.RemoteDbRepository
import com.nagel.wordnotification.data.premium.PremiumRepository
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.presentation.base.BaseViewModel
import com.nagel.wordnotification.presentation.navigator.NavigatorV2
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LibraryDictionariesVM @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
    private val realtimeDb: RemoteDbRepository,
    private var navigatorV2: NavigatorV2,
    private val sessionRepository: SessionRepository,
    private val premiumRepository: PremiumRepository
) : BaseViewModel() {

    private var listDictionaryChecked = mutableSetOf<Dictionary>()
    private val _localState = MutableStateFlow(DictionariesLibraryScreenState())
    val state = _localState

    init {
        if (state.value.dictionariesList == null) {
            loadDictionaries()
        }
    }

    fun loadDictionaries() {
        realtimeDb.requestGetDictionaries(
            success = {
                _localState.value = _localState.value.copy(
                    isLoading = false,
                    dictionariesList = it,
                    isError = false
                )
            },
            error = {
                _localState.value = _localState.value.copy(
                    isLoading = false,
                    dictionariesList = null,
                    isError = true
                )
            }
        )
    }

    fun changeChecked(dictionary: Dictionary, isCheck: Boolean) {
        if (isCheck) {
            listDictionaryChecked.add(dictionary)
        } else {
            listDictionaryChecked.remove(dictionary)
        }
        _localState.value = _localState.value.copy(showAddButton = listDictionaryChecked.size != 0)
    }

    fun addToDbDictionaries() {
        val names = listDictionaryChecked.map { it.name }
        names.forEach { item ->
            AppMetricaAnalytic.reportEvent("library_add_dictionary", mapOf("name" to item))
        }
        _localState.value = _localState.value.copy(isLoading = true)
        viewModelScope.launch(Dispatchers.IO) {
            listDictionaryChecked.forEach {
                dictionaryRepository.saveDictionary(it)
            }
            val currentLimit = premiumRepository.getCurrentLimitWord()
            val newLimit = currentLimit + listDictionaryChecked.sumOf { it.wordList.size }
            premiumRepository.saveCurrentLimitWords(newLimit)
            listDictionaryChecked.clear()
            withContext(Dispatchers.Main) {
                navigatorV2.toast(R.string.import_success)
            }
            _localState.emit(_localState.value.copy(isLoading = false, closeAction = true))
        }
    }

}