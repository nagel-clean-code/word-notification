package com.nagel.wordnotification.presentation.choosingdictionary.library

import com.nagel.wordnotification.data.firbase.RealtimeDbRepository

data class DictionariesLibraryScreenState(
    var isLoading: Boolean = false,
    var dictionariesList: RealtimeDbRepository.DictionariesLibrary? = null,
    var isError: Boolean = false,
    val showAddButton: Boolean = false,
    val closeAction: Boolean = false
)