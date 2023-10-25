package com.nagel.wordnotification.data.dictionaries

import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.dictionaries.room.entities.WordDbEntity
import kotlinx.coroutines.flow.Flow

interface DictionaryRepository {
    fun loadDictionaries(accountId: Long): Flow<List<Dictionary>>
    suspend fun getWordsByIdDictionary(idDictionary: Long): List<Word>
    fun loadDictionaryByName(name: String, idAuthor: Long, success: (Boolean) -> Unit)
    fun loadDictionaryById(idDictionary: Long, success: (Boolean) -> Unit)
    fun deleteWordById(idWord: Long, success: (Boolean) -> Unit)
    fun getSize(): Int
    fun getItem(position: Int): Word
    fun addWord(textFirst: String, textLast: String)
    fun createDictionary(name: String, idAccount: Long, success: () -> Unit = {})
    fun updateWord(word: Word): Word?
}