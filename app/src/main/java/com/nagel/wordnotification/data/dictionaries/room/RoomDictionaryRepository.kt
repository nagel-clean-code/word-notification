package com.nagel.wordnotification.data.dictionaries.room

import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.dictionaries.room.entities.DictionaryDbEntity
import com.nagel.wordnotification.data.dictionaries.room.entities.WordDbEntity
import com.nagel.wordnotification.data.wrapSQLiteException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomDictionaryRepository @Inject constructor(
    private val dictionaryDao: DictionaryDao,
) : DictionaryRepository {

    private var currentDictionary: Dictionary? = null

    override fun loadDictionary(name: String, idAuthor: Long, success: (Boolean) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            val dictionaryDbEntity = dictionaryDao.getDictionary(name, idAuthor)
            currentDictionary = dictionaryDbEntity?.toDictionary()
            currentDictionary?.wordList = getWords().map { it.toWord() }.toMutableList()
            success.invoke(currentDictionary != null)
        }
    }

    private suspend fun getWords(): List<WordDbEntity> {
        return dictionaryDao.getWords(currentDictionary!!.idDictionaries)
    }

    override fun getSize(): Int {
        return currentDictionary!!.wordList.size
    }

    override fun getItem(position: Int): Word {
        return currentDictionary!!.wordList[currentDictionary!!.wordList.size - position-1]
    }

    override fun addWord(textFirst: String, textLast: String) {
        val word =
            WordDbEntity.createWordDbEntity(textFirst, textLast, currentDictionary!!.idDictionaries)
        currentDictionary!!.wordList.add(word.toWord())
        GlobalScope.launch {
            wrapSQLiteException(Dispatchers.IO) {
                dictionaryDao.addWord(word)
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