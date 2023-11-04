package com.nagel.wordnotification.data.dictionaries.entities

import com.nagel.wordnotification.utils.GlobalFunction


data class Word(
    val idDictionary: Long,
    val textFirst: String,
    val textLast: String,
    var learned: Boolean = false,
    var learnStep: Int = 0,
    var lastDateMention: Long = 0,
    var uniqueId: Int = GlobalFunction.generateUniqueId()
) {
    var idWord: Long = 0
}