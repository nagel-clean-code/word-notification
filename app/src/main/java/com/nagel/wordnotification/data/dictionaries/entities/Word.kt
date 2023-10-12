package com.nagel.wordnotification.data.dictionaries.entities

data class Word(
    val idDictionary: Long,
    val textFirst: String,
    val textLast: String,
    val learned: Boolean,
) {
    var idWord: Long = 0
}