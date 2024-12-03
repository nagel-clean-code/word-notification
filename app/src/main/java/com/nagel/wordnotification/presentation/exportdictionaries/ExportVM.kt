package com.nagel.wordnotification.presentation.exportdictionaries

import androidx.lifecycle.viewModelScope
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.presentation.base.BaseViewModel
import com.nagel.wordnotification.presentation.exportAndImport.ExportGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ExportVM @Inject constructor(
    private val exportGenerator: ExportGenerator,
    private val dictionaryRepository: DictionaryRepository,
    private val sessionRepository: SessionRepository
) : BaseViewModel() {

    var isStarted: Boolean = false

    fun writeDictionaries(isAlgorithm: Boolean, sendFile: (File) -> Unit) {
        val accountId = sessionRepository.getAccountId() ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val list = dictionaryRepository.loadDictionaries(accountId)
            val file = exportGenerator.writeDictionaries(list, isAlgorithm)
            withContext(Dispatchers.Main) {
                sendFile(file)
            }
        }
    }
}