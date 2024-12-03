package com.nagel.wordnotification.data.dictionaries.entities

data class NotificationHistoryItem(
    val idNotification: Long = 0,
    var idWord: Long,
    val dateMention: Long,
    var idMode: Long,
    val learnStep: Int
)