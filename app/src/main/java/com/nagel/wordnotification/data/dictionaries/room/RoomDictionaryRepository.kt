package com.nagel.wordnotification.data.dictionaries.room

import android.util.Log
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.dictionaries.room.entities.DictionaryDbEntity
import com.nagel.wordnotification.data.dictionaries.room.entities.WordDbEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
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

    override fun loadDictionaries(accountId: Long): Flow<List<Dictionary>> {
        return dictionaryDao.getMyDictionaries(accountId).map { itFlow ->
            itFlow?.map {
                val result = it.toDictionary()
                result.wordList = getWordsByIdDictionary(result.idDictionary).toMutableList()
                result
            } ?: listOf()
        }
    }

    override fun loadDictionaryByName(
        name: String,
        idAuthor: Long,
        success: (Dictionary?) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch() {
            val dictionaryDbEntity = dictionaryDao.getDictionaryByName(name, idAuthor)
            val currentDictionary = dictionaryDbEntity?.toDictionary()
            currentDictionary?.wordList =
                getWords(currentDictionary!!.idDictionary).map { it.toWord() }.toMutableList()
            success.invoke(currentDictionary)
        }
    }

    override fun loadDictionaryById(idDictionary: Long, success: (Dictionary?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch() {
            val currentDictionary = dictionaryDao.getDictionaryById(idDictionary)?.toDictionary()
            currentDictionary?.wordList = getWords(idDictionary).map { it.toWord() }.toMutableList()
            success.invoke(currentDictionary)
        }
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

    override fun addWord(wordDto: Word, success: (Long) -> Unit) {
        val word = WordDbEntity.createWordDbEntity(wordDto)
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                val id = dictionaryDao.addWord(word)
                withContext(Dispatchers.Main) {
                    success.invoke(id)
                }
            }
        }
    }

    override suspend fun updateWord(word: Word) {
        dictionaryDao.updateWord(WordDbEntity.createWordDbEntity(word))
    }

    override suspend fun updateIncludeDictionary(include: Boolean, idDictionary: Long) {
        dictionaryDao.setInclude(idDictionary, include)
    }

    override fun createDictionary(
        name: String,
        idAccount: Long,
        include: Boolean,
        success: (dictionary: Dictionary) -> Unit,
    ) {
        GlobalScope.launch {
            val dictionaryDbEntity =
                DictionaryDbEntity.createDictionary(
                    name,
                    idFolder = 0,
                    idAuthor = idAccount,
                    included = include
                )
            withContext(Dispatchers.IO) {
                val id = dictionaryDao.saveDictionary(dictionaryDbEntity)
                val currentDictionary = dictionaryDbEntity.toDictionary()
                currentDictionary.idDictionary = id
                withContext(Dispatchers.Main) {
                    success.invoke(currentDictionary)
                }
            }
        }
    }

}