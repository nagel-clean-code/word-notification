package com.nagel.wordnotification.data.dictionaries.entities

import com.nagel.wordnotification.data.dictionaries.room.entities.DictionaryDbEntity
import java.util.Date

data class Dictionary(
    var idDictionary: Long,
    val idAuthor: Long,
    val name: String,
    val dateCreated: Long,
    val idFolder: Long,
    var idMode: Long,
    var wordList: MutableList<Word> = mutableListOf(),
    var include: Boolean //Включён ли алгоритм
) {

    fun toDbEntity(): DictionaryDbEntity {
        return DictionaryDbEntity(
            id = idDictionary,
            idAuthor = idAuthor,
            name = name,
            dateCreated = dateCreated,
            idFolder = idFolder,
            idMode = idMode,
            included = include
        )
    }

    companion object {
        fun createEmpty(name: String) = Dictionary(
            idDictionary = 0,
            idAuthor = 0,
            name = name,
            dateCreated = Date().time,
            idFolder = 0,
            idMode = 0,
            include = false
        )
    }
}