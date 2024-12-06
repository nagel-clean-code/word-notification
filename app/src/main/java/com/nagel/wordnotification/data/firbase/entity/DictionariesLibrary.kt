package com.nagel.wordnotification.data.firbase.entity

import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.presentation.exportAndImport.CashReader

data class DictionariesLibrary(
    val contents: String
) {
    constructor() : this("")

    suspend fun getDictionaries(dataReader: CashReader): List<Dictionary> {
        return dataReader.fireReader(contents)
    }
}