package com.nagel.wordnotification.data.dictionaries.entities

data class NotificationHistoryItem(
    val idNotification: Long = 0,
    val idWord: Long,
    val dateMention: Long,
    val idMode: Long,
    val learnStep: Int
)