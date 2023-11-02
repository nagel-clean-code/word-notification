package com.nagel.wordnotification.data.dictionaries

import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.dictionaries.room.entities.WordDbEntity
import kotlinx.coroutines.flow.Flow

interface DictionaryRepository {
    fun loadDictionaries(accountId: Long): Flow<List<Dictionary>>
    suspend fun getWordsByIdDictionary(idDictionary: Long): List<Word>
    fun loadDictionaryByName(name: String, idAuthor: Long, success: (Dictionary?) -> Unit)
    fun loadDictionaryById(idDictionary: Long, success: (Dictionary?) -> Unit)
    fun deleteWordById(idWord: Long, success: (Boolean) -> Unit)
    fun deleteDictionaryById(idDictionary: Long, success: (Boolean) -> Unit)
    fun addWord(word: Word, success: (Long) -> Unit)
    fun createDictionary(name: String, idAccount: Long, success: (dictionary: Dictionary) -> Unit = {})
}