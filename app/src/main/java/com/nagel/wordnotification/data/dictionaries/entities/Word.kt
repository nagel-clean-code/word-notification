package com.nagel.wordnotification.data.dictionaries.entities

import com.nagel.wordnotification.utils.GlobalFunction
import java.util.Date


data class Word(
    val idDictionary: Long,
    val textFirst: String,
    val textLast: String,
    var allNotificationsCreated: Boolean = false,
    var learnStep: Int = 0,
    var lastDateMention: Long = 0,
    var uniqueId: Int = GlobalFunction.generateUniqueId()
) {
    var idWord: Long = 0

    fun isItWasRepeated() = allNotificationsCreated && lastDateMention < Date().time
}