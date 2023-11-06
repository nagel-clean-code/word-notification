package com.nagel.wordnotification.core.services

import android.app.NotificationManager
import android.content.Context
import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.dictionaries.entities.Word

object Utils {

    fun deleteNotification(context: Context, word: Word) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        for (i in (0..MAX_NUMBER_NOTIFICATION_CREATED)) {
            manager?.cancel(word.uniqueId + word.learnStep - i)
        }
    }

    fun deleteNotification(context: Context, dictionary: Dictionary) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        dictionary.wordList.forEach { word ->
            for (i in (0..MAX_NUMBER_NOTIFICATION_CREATED)) {
                manager?.cancel(word.uniqueId + word.learnStep - i)
            }
        }
    }

    private const val MAX_NUMBER_NOTIFICATION_CREATED = 8
}