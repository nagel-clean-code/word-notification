package com.nagel.wordnotification.presentation.choosingdictionary.library

import androidx.lifecycle.viewModelScope
import com.nagel.wordnotification.R
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.firbase.RemoteDbRepository
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.presentation.base.BaseViewModel
import com.nagel.wordnotification.presentation.navigator.NavigatorV2
import dagger.hilt.android.lifecycle.HiltViewModel
import io.appmetrica.analytics.AppMetrica
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
    private val sessionRepository: SessionRepository
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

    private fun combineStates(
        state1: RemoteDbRepository.DictionariesLibraryState,
        state2: DictionariesLibraryScreenState
    ): DictionariesLibraryScreenState {
        if (state2.isLoading || state1.isLoading) {
            state2.isLoading = true
        }
        if (state2.isError || state1.isError) {
            state2.isError = true
        }
        state2.dictionariesList = state1.dictionariesList
        return state2
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
            AppMetrica.reportEvent("library_add_dictionary", mapOf("name" to item))
        }
        _localState.value = _localState.value.copy(isLoading = true)
        viewModelScope.launch(Dispatchers.IO) {
            listDictionaryChecked.forEach {
                dictionaryRepository.saveDictionary(it)
            }
            val currentLimit = sessionRepository.getLimitWord()
            val newLimit = currentLimit + listDictionaryChecked.sumOf { it.wordList.size }
            sessionRepository.changLimitWords(newLimit)
            listDictionaryChecked.clear()
            withContext(Dispatchers.Main) {
                navigatorV2.toast(R.string.import_success)
            }
            _localState.emit(_localState.value.copy(isLoading = false, closeAction = true))
        }
    }

}