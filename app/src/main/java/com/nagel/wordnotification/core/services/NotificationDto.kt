package com.nagel.wordnotification.core.services

import java.text.SimpleDateFormat
import java.util.Date

data class NotificationDto(
    var text: String?,
    var translation: String?,
    val date: Long,
    var uniqueId: Int,
    var step: Int
) {

    override fun toString(): String {
        return "NotificationDto(text=$text, translation=$translation, date=" + dateFormat.format(
            Date(date)
        ) + ", uniqueId=$uniqueId, step=$step)"
    }

    companion object {
        val dateFormat = SimpleDateFormat("d, HH:mm:ss")
    }
}