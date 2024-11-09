package com.nagel.wordnotification.data.dictionaries

import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.dictionaries.entities.NotificationHistoryItem
import com.nagel.wordnotification.data.dictionaries.entities.Word
import kotlinx.coroutines.flow.Flow

interface DictionaryRepository : IFormationOfDictionaries {
    fun loadDictionariesFlow(accountId: Long): Flow<List<Dictionary>>
    fun loadWordsByIdDictionaryFlow(idDictionary: Long): Flow<List<Word>>
    fun getAllWordsFlow(): Flow<List<Word>>
    suspend fun getAllWords(): List<Word>
    suspend fun loadDictionaries(accountId: Long): List<Dictionary>
    suspend fun getWordsByIdDictionary(idDictionary: Long): List<Word>
    suspend fun updateNameDictionary(idDictionary: Long, name: String)
    suspend fun loadDictionaryByName(name: String, idAuthor: Long): Dictionary?
    suspend fun loadDictionaryById(idDictionary: Long): Dictionary?
    suspend fun deleteWordById(idWord: Long): Int
    fun deleteDictionaryById(idDictionary: Long, success: (Boolean) -> Unit)
    suspend fun updateWord(word: Word)
    suspend fun updateText(word: Word)
    suspend fun updateIncludeDictionary(include: Boolean, idDictionary: Long)
    suspend fun saveNotificationHistoryItem(notification: NotificationHistoryItem)
    suspend fun deleteNotificationHistoryItem(notification: NotificationHistoryItem): Int
    fun loadHistoryNotificationFlow(
        idWord: Long,
        idMode: Long
    ): Flow<List<NotificationHistoryItem>?>

    suspend fun loadHistoryNotification(idWord: Long, idMode: Long): List<NotificationHistoryItem>?
    suspend fun saveDictionary(dto: Dictionary): Long
}

interface IFormationOfDictionaries {
    suspend fun createDictionary(
        name: String,
        idAccount: Long,
        include: Boolean = false,
    ): Dictionary

    suspend fun addWord(word: Word): Long
}