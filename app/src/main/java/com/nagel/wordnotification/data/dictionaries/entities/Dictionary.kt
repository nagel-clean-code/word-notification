package com.nagel.wordnotification.data.dictionaries.entities

data class Dictionary(
    var idDictionaries: Long,
    val idAuthor: Long,
    val name: String,
    val dateCreated: Long,
    val idFolder: Long,
    val mode: Long,
    var wordList: MutableList<Word> = mutableListOf(),
    var include: Boolean
)