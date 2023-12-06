package com.nagel.wordnotification.data.dictionaries

import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.dictionaries.entities.NotificationHistoryItem
import com.nagel.wordnotification.data.dictionaries.entities.Word
import kotlinx.coroutines.flow.Flow

interface DictionaryRepository {
    fun loadDictionariesFlow(accountId: Long): Flow<List<Dictionary>>
    suspend fun loadDictionaries(accountId: Long): List<Dictionary>
    suspend fun getWordsByIdDictionary(idDictionary: Long): List<Word>
    suspend fun updateNameDictionary(idDictionary: Long, name: String)
    fun loadDictionaryByName(name: String, idAuthor: Long, success: (Dictionary?) -> Unit)
    fun loadDictionaryById(idDictionary: Long, success: (Dictionary?) -> Unit)
    suspend fun deleteWordById(idWord: Long): Int
    fun deleteDictionaryById(idDictionary: Long, success: (Boolean) -> Unit)
    suspend fun addWord(word: Word): Long
    suspend fun updateWord(word: Word)
    suspend fun updateText(word: Word)
    suspend fun updateIncludeDictionary(include: Boolean, idDictionary: Long)
    suspend fun saveNotificationHistoryItem(notification: NotificationHistoryItem)
    fun loadHistoryNotification(
        idWord: Long,
        idMode: Long
    ): Flow<List<NotificationHistoryItem>?>

    suspend fun createDictionary(
        name: String,
        idAccount: Long,
        include: Boolean = false,
    ): Dictionary
}