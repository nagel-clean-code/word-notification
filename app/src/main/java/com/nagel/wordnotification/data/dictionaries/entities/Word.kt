package com.nagel.wordnotification.data.dictionaries.entities

data class Word(
    val idWord: Long,
    val textFirst: String,
    val textLast: String,
    val learned: Boolean,
)