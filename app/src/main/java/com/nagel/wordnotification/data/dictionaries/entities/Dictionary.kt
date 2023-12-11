package com.nagel.wordnotification.data.dictionaries.entities

data class Dictionary(
    var idDictionary: Long,
    val idAuthor: Long,
    val name: String,
    val dateCreated: Long,
    val idFolder: Long,
    val idMode: Long,
    var wordList: MutableList<Word> = mutableListOf(),
    var include: Boolean
)