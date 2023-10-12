package com.nagel.wordnotification.data.dictionaries.room

import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.dictionaries.room.entities.DictionaryDbEntity
import com.nagel.wordnotification.data.dictionaries.room.entities.WordDbEntity
import com.nagel.wordnotification.data.wrapSQLiteException
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

    private var currentDictionary: Dictionary? = null
    override fun loadDictionaries(accountId: Long): Flow<List<Dictionary>> {
        return dictionaryDao.getMyDictionaries(accountId).map { itFlow ->
            itFlow?.map { it.toDictionary() } ?: listOf()
        }
    }

    override fun loadDictionaryByName(name: String, idAuthor: Long, success: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch() {
            val dictionaryDbEntity = dictionaryDao.getDictionaryByName(name, idAuthor)
            currentDictionary = dictionaryDbEntity?.toDictionary()
            currentDictionary?.wordList = getWords().map { it.toWord() }.toMutableList()
            success.invoke(currentDictionary != null)
        }
    }

    override fun loadDictionaryById(idDictionary: Long, success: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch() {
            currentDictionary = dictionaryDao.getDictionaryById(idDictionary)?.toDictionary()
            currentDictionary?.wordList = getWords().map { it.toWord() }.toMutableList()
            success.invoke(currentDictionary != null)
        }
    }

    override fun deleteWordById(idWord: Long, success: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch() {
            val count = dictionaryDao.deleteWord(idWord)
            withContext(Dispatchers.Main) {
                if (count > 0) {
                    currentDictionary!!.wordList.removeIf { it.idWord == idWord }
                }
                success.invoke(count > 0)
            }
        }
    }

    private suspend fun getWords(): List<WordDbEntity> {
        return dictionaryDao.getWords(currentDictionary!!.idDictionaries)
    }

    override fun getSize(): Int {
        return currentDictionary?.wordList?.size ?: 0
    }

    override fun getItem(position: Int): Word {
        return currentDictionary!!.wordList[currentDictionary!!.wordList.size - position - 1]
    }

    override fun updateWord(word: Word): Word? {
        return currentDictionary!!.wordList.find { word.hashCode() == it.hashCode() }
    }

    override fun addWord(textFirst: String, textLast: String) {
        val word =
            WordDbEntity.createWordDbEntity(textFirst, textLast, currentDictionary!!.idDictionaries)
        currentDictionary!!.wordList.add(word.toWord())
        GlobalScope.launch {
            wrapSQLiteException(Dispatchers.IO) {
                val id = dictionaryDao.addWord(word)
                currentDictionary!!.wordList.last().idWord = id
            }
        }
    }

    override fun createDictionary(name: String, idAccount: Long, success: () -> Unit) {
        GlobalScope.launch {
            val dictionaryDbEntity =
                DictionaryDbEntity.createDictionary(name, idFolder = 0, idAuthor = idAccount)
            wrapSQLiteException(Dispatchers.IO) {
                val id = dictionaryDao.saveDictionary(dictionaryDbEntity)
                currentDictionary = dictionaryDbEntity.toDictionary()
                currentDictionary!!.idDictionaries = id
                withContext(Dispatchers.Main) {
                    success.invoke()
                }
            }
        }
    }

}