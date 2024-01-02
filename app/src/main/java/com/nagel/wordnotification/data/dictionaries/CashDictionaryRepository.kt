package com.nagel.wordnotification.data.dictionaries

import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.dictionaries.room.entities.DictionaryDbEntity
import com.nagel.wordnotification.data.dictionaries.room.entities.WordDbEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CashDictionaryRepository @Inject constructor() : IFormationOfDictionaries {

    private var idCounter = 0L
    private val dictionaryList = mutableListOf<Dictionary>()

    override suspend fun addWord(word: Word): Long {
        val newWord = WordDbEntity.createWordDbEntity(word).toWord()
        dictionaryList.find { it.idDictionary == word.idDictionary }?.wordList?.add(newWord)
        return 0
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
        val currentDictionary = dictionaryDbEntity.toDictionary()
        currentDictionary.idDictionary = idCounter++
        dictionaryList.add(currentDictionary)
        return currentDictionary
    }

    fun getDictionaries(): List<Dictionary> {
        return dictionaryList
    }
}