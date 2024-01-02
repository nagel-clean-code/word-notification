package com.nagel.wordnotification.presentation.reader

import com.nagel.wordnotification.data.dictionaries.CashDictionaryRepository
import javax.inject.Inject

class ImportInCash @Inject constructor(
    val dictionaryRepository: CashDictionaryRepository
) : FileReader(dictionaryRepository)