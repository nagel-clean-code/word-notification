package com.nagel.wordnotification.data.dictionaries.room

import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.dictionaries.entities.NotificationHistoryItem
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.dictionaries.room.entities.DictionaryDbEntity
import com.nagel.wordnotification.data.dictionaries.room.entities.NotificationHistoryDbEntity
import com.nagel.wordnotification.data.dictionaries.room.entities.WordDbEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomDictionaryRepository @Inject constructor(
    private val dictionaryDao: DictionaryDao,
) : DictionaryRepository {

    override fun loadDictionariesFlow(accountId: Long): Flow<List<Dictionary>> {
        return dictionaryDao.getMyDictionariesFlow(accountId).map { itFlow ->
            itFlow?.map {
                val result = it.toDictionary()
                result.wordList = getWordsByIdDictionary(result.idDictionary).toMutableList()
                result
            } ?: listOf()
        }
    }

    override fun loadWordsByIdDictionaryFlow(idDictionary: Long): Flow<List<Word>> {
        return dictionaryDao.getWordsFlow(idDictionary).map { itFlow ->
            itFlow?.map { it.toWord() } ?: listOf()
        }
    }

    override suspend fun loadDictionaries(accountId: Long): List<Dictionary> {
        return dictionaryDao.getMyDictionaries(accountId)?.map { dictionary ->
            val result = dictionary.toDictionary()
            result.wordList = getWordsByIdDictionary(result.idDictionary).toMutableList()
            result
        } ?: listOf()
    }

    override suspend fun loadDictionaryByName(
        name: String,
        idAuthor: Long
    ): Dictionary? {
        val dictionaryDbEntity = dictionaryDao.getDictionaryByName(name, idAuthor)
        val currentDictionary = dictionaryDbEntity?.toDictionary()
        currentDictionary?.wordList =
            getWords(currentDictionary!!.idDictionary).map { it.toWord() }.toMutableList()
        return currentDictionary
    }

    override suspend fun loadDictionaryById(idDictionary: Long): Dictionary? {
        val currentDictionary = dictionaryDao.getDictionaryById(idDictionary)?.toDictionary()
        currentDictionary?.wordList = getWords(idDictionary).map { it.toWord() }.toMutableList()
        return currentDictionary
    }

    override suspend fun deleteWordById(idWord: Long): Int {
        return dictionaryDao.deleteWord(idWord)
    }

    override fun deleteDictionaryById(idDictionary: Long, success: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch() {
            val count = dictionaryDao.deleteDictionary(idDictionary)
            withContext(Dispatchers.Main) {
                success.invoke(count > 0)
            }
        }
    }

    private suspend fun getWords(idDictionary: Long): List<WordDbEntity> {
        return dictionaryDao.getWords(idDictionary)
    }

    override suspend fun getWordsByIdDictionary(idDictionary: Long): List<Word> {
        return dictionaryDao.getWords(idDictionary).map { it.toWord() }
    }

    override suspend fun updateNameDictionary(idDictionary: Long, name: String) {
        dictionaryDao.updateDictionaryName(name, idDictionary)
    }

    override suspend fun addWord(word: Word): Long {
        val word = WordDbEntity.createWordDbEntity(word)
        return dictionaryDao.addWord(word)
    }

    override suspend fun updateWord(word: Word) {
        dictionaryDao.updateWord(WordDbEntity.createWordDbEntity(word))
    }

    override suspend fun updateText(word: Word) {
        dictionaryDao.updateTextInWord(word.idWord, word.textFirst, word.textLast, word.uniqueId)
    }

    override suspend fun updateIncludeDictionary(include: Boolean, idDictionary: Long) {
        dictionaryDao.setInclude(idDictionary, include)
    }

    override suspend fun saveNotificationHistoryItem(notification: NotificationHistoryItem) {
        val data = NotificationHistoryDbEntity.createNotificationHistoryDbEntity(notification)
        dictionaryDao.saveNotificationHistoryItem(data)
    }

    override suspend fun deleteNotificationHistoryItem(notification: NotificationHistoryItem): Int {
        return dictionaryDao.deleteNotificationHistoryItem(notification.idNotification)
    }

    override fun loadHistoryNotificationFlow(
        idWord: Long,
        idMode: Long
    ): Flow<List<NotificationHistoryItem>?> {
        return dictionaryDao.getNotificationHistoryFlow(idWord, idMode).map { flow ->
            flow?.map { it.toNotificationHistoryItem() }
        }
    }

    override suspend fun loadHistoryNotification(
        idWord: Long,
        idMode: Long
    ): List<NotificationHistoryItem>? {
        return dictionaryDao.getNotificationHistory(idWord, idMode)?.map {
            it.toNotificationHistoryItem()
        }
    }

    override suspend fun createDictionary(
        name: String,
        idAccount: Long,
        include: Boolean,
    ): Dictionary {
        val dictionaryDbEntity =
            DictionaryDbEntity.createDictionary(
                name,
                idFolder = 0,
                idAuthor = idAccount,
                included = include
            )
        val id = dictionaryDao.saveDictionary(dictionaryDbEntity)
        val currentDictionary = dictionaryDbEntity.toDictionary()
        currentDictionary.idDictionary = id
        return currentDictionary
    }

}