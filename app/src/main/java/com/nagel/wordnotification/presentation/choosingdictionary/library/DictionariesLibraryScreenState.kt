package com.nagel.wordnotification.presentation.choosingdictionary.library

import com.nagel.wordnotification.data.firbase.entity.DictionariesLibrary

data class DictionariesLibraryScreenState(
    var isLoading: Boolean = true,
    var dictionariesList: DictionariesLibrary? = null,
    var isError: Boolean = false,
    val showAddButton: Boolean = false,
    val closeAction: Boolean = false
)