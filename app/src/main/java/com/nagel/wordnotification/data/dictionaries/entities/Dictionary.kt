package com.nagel.wordnotification.data.dictionaries.entities

import com.nagel.wordnotification.data.dictionaries.room.entities.DictionaryDbEntity

data class Dictionary(
    var idDictionary: Long,
    val idAuthor: Long,
    val name: String,
    val dateCreated: Long,
    val idFolder: Long,
    val idMode: Long,
    var wordList: MutableList<Word> = mutableListOf(),
    var include: Boolean
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
}