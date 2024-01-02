package com.nagel.wordnotification.presentation.reader

import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import javax.inject.Inject

class ImportInDb @Inject constructor(
    dictionaryRepository: DictionaryRepository
) : FileReader(dictionaryRepository)