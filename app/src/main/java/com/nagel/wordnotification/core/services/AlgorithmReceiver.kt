package com.nagel.wordnotification.core.services

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.util.Log
import com.nagel.wordnotification.Constants.TAKE_AWAY
import com.nagel.wordnotification.Constants.TYPE
import com.nagel.wordnotification.Constants.TYPE_ANSWER
import com.nagel.wordnotification.Constants.TYPE_QUEST
import com.nagel.wordnotification.core.algorithms.NotificationAlgorithm
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.NotificationHistoryItem
import com.nagel.wordnotification.data.session.SessionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class AlgorithmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var sessionRepository: SessionRepository

    @Inject
    lateinit var dictionaryRepository: DictionaryRepository

    @Inject
    lateinit var notificationAlgorithm: NotificationAlgorithm

    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("CoroutineWorker:", "AlgorithmReceiver: start")
        val pendingResult = goAsync()
        GlobalScope.launch(Dispatchers.Default) {
            try {
                val type = intent.getIntExtra(TYPE, 0)
                val json = intent.getStringExtra(TAKE_AWAY)
                val notificationDto = Utils.getDtoFromJson(context, intent)
                if (notificationDto != null) {
                    if (type == TYPE_ANSWER) {
                        val newIntent = Intent(context, AlarmReceiver::class.java)
                        newIntent.putExtra(TAKE_AWAY, json)
                        newIntent.putExtra(TYPE, TYPE_QUEST)
                        context.sendBroadcast(newIntent)
                        return@launch
                    } else {
                        updateCurrentWord(notificationDto)
                        saveNotificationHistoryItem(notificationDto)
                        notificationAlgorithm.createNotification()
                    }
                    Log.d("CoroutineWorker:", "notificationDto: $notificationDto")
                    val manager =
                        context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
                    manager?.cancel(notificationDto.uniqueId + notificationDto.step)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun updateCurrentWord(notification: NotificationDto) {
        val word = dictionaryRepository.getWordByUniqueId(notification.uniqueId)
        word?.let {
            ++word.learnStep
            word.lastDateMention = Date().time
            dictionaryRepository.updateWord(word)
        }
    }

    private suspend fun saveNotificationHistoryItem(notificationDto: NotificationDto) {
        val word = dictionaryRepository.getWordByUniqueId(notificationDto.uniqueId)
        word?.let {
            dictionaryRepository.loadDictionaryById(word.idDictionary)?.let {
                val historyItem = NotificationHistoryItem(
                    0,
                    word.idWord,
                    Date().time,
                    it.idMode,
                    notificationDto.step
                )
                Log.d("HISTORY::", "saveNotificationHistoryItem historyItem:$historyItem")
                dictionaryRepository.saveNotificationHistoryItem(historyItem)
            }
        }
    }
}