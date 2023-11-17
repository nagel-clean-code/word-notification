package com.nagel.wordnotification.core.algorithms

import android.util.Log
import com.nagel.wordnotification.core.services.NotificationDto
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.data.settings.SettingsRepository
import com.nagel.wordnotification.data.settings.entities.ModeSettingsDto
import com.nagel.wordnotification.data.settings.room.entities.ModeDbEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class NotificationAlgorithm @Inject constructor(
    private var sessionRepository: SessionRepository,
    private var settingsRepository: SettingsRepository,
    var dictionaryRepository: DictionaryRepository,
) {

    val wordsForNotifications = MutableStateFlow<List<NotificationDto?>?>(null)
    private lateinit var bufArray: ArrayList<NotificationDto?>
    private var countFirstNotifications = 0

    suspend fun start() {
        wordsForNotifications.emit(null)
        bufArray = arrayListOf()
        countFirstNotifications = 0
        loadWords()
    }

    private suspend fun loadWords() {
        CoroutineScope(Dispatchers.IO).launch {
            val accountId = sessionRepository.getSession()?.account?.id
            Log.d("CoroutineWorker:", "accountId:${accountId}")
            accountId?.let { id ->
                dictionaryRepository.loadDictionaries(id).collect() { dictionaries ->
                    Log.d("CoroutineWorker:", "dictionariesSize:${dictionaries.size}")
                    dictionaries.forEach {
                        if (it.include) {
                            initNotifications(it)
                        }
                    }
                    wordsForNotifications.emit(bufArray.toList())
                }
            }
        }
    }

    private suspend fun initNotifications(dictionary: Dictionary) {
        val mode = settingsRepository.getModeSettings(dictionary.idDictionaries)
        if (mode == null) {
            Log.d("CoroutineWorker:", "mode == null")
            return
        }
        dictionary.wordList.filter { !it.allNotificationsCreated && it.lastDateMention < Date().time }
            .forEach {
                if (it.learnStep == 0) {
                    //Добавление интервала между словами на первом шаге, чтобы не появились все в один раз
                    it.lastDateMention = countFirstNotifications++ * getIntervalBetweenWords()
                }
                var nextTime = getNewDate(mode, it.learnStep++, it.lastDateMention)
                do {
                    val d = if (nextTime == null) {
                        it.allNotificationsCreated = true
                        null
                    } else {
                        it.lastDateMention = nextTime
                        NotificationDto(
                            it.textFirst,
                            it.textLast,
                            nextTime,
                            it.uniqueId,
                            it.learnStep
                        )
                    }
                    nextTime = if (nextTime == null ||
                        !checkOccurrenceInTimeInterval(nextTime, mode.toMode())
                    ) {
                        it.learnStep--
                        updateWord(it)
                        null
                    } else {
                        updateWord(it)
                        bufArray.add(d)
                        getNewDate(mode, it.learnStep++, it.lastDateMention)
                    }
                } while (nextTime != null && nextTime - Date().time < MAX_WORKER_RESTART_INTERVAL)
            }
    }

    private fun checkOccurrenceInTimeInterval(time: Long, mode: ModeSettingsDto): Boolean {
        var timeInterval = true
        if (mode.timeIntervals) {
            timeInterval = checkMinutes(mode, time)
        }

        var daysSelected = true
        if (mode.sampleDays) {
            val day = SimpleDateFormat("EE").format(Date(time))
            val str = day[0].uppercase() + day[1]
            daysSelected = mode.days.contains(str)
        }
        return timeInterval && daysSelected
    }

    private fun checkMinutes(mode: ModeSettingsDto, time: Long): Boolean {
        val startInterval = mode.workingTimeInterval.first
        val endInterval = mode.workingTimeInterval.second

        val sIHour = startInterval.substring(0, startInterval.indexOf(':')).toInt()
        val sIMinutes = startInterval.substring(startInterval.indexOf(':') + 1).toInt()

        val eIHour = endInterval.substring(0, endInterval.indexOf(':')).toInt()
        val eIMinutes = endInterval.substring(endInterval.indexOf(':') + 1).toInt()

        val c = Calendar.getInstance()
        c.time = Date(time)
        val hours = c.get(Calendar.HOUR_OF_DAY)
        val minutes = c.get(Calendar.MINUTE)

        var timeInterval = true
        if (sIHour < eIHour) {
            if (hours < sIHour || hours > eIHour) {
                timeInterval = false
            } else if (hours == sIHour && minutes < sIMinutes) {
                timeInterval = false
            } else if (hours == eIHour && minutes > eIMinutes) {
                timeInterval = false
            }
        } else if (sIHour > eIHour) {
            if (hours < sIHour && hours > eIHour) {
                timeInterval = false
            } else if (hours == sIHour && minutes < sIMinutes) {
                timeInterval = false
            } else if (hours == eIHour && minutes > eIMinutes) {
                timeInterval = false
            }
        } else {
            if (hours != sIHour) {
                timeInterval = false
            } else if (sIMinutes < eIMinutes) {
                if (minutes < sIMinutes || minutes > eIMinutes) {
                    timeInterval = false
                }
            } else {
                if (minutes > sIMinutes || minutes < eIMinutes) {
                    timeInterval = false
                }
            }
        }
        val dateTime = dateFormat.format(Date(time))
        Log.d(
            "CoroutineWorker:",
            "CHECK_TIME: time = $dateTime, " +
                    "timeInterval = $timeInterval, " +
                    "startInterval = $startInterval, " +
                    "endInterval = $endInterval"
        )
        return timeInterval
    }

    private fun getNewDate(mode: ModeDbEntity, step: Int, lastDate: Long): Long? {
        val time = when (mode.selectedMode) {
            PlateauEffect::class.simpleName -> {
                Log.d("CoroutineWorker:", "AlgorithmPlateauEffect")
                PlateauEffect.getNewDate(step, lastDate)
            }

            ForgetfulnessCurveLong::class.simpleName -> {
                Log.d("CoroutineWorker:", "AlgorithmForgetfulnessCurveLong")
                ForgetfulnessCurveLong.getNewDate(step, lastDate)
            }

            ForgetfulnessCurveShort::class.simpleName -> {
                Log.d("CoroutineWorker:", "ForgetfulnessCurveShort")
                ForgetfulnessCurveShort.getNewDate(step, lastDate)
            }

            else -> {
                null
            }
        }
        Log.d(
            "CoroutineWorker:",
            "nextTime = ${time?.let { dateFormat.format(Date(time)) }}"
        )
        return time
    }

    private suspend fun updateWord(word: Word) {
        dictionaryRepository.updateWord(word)
    }


    companion object {
        val dateFormat = SimpleDateFormat("d, hh:mm:ss")

        const val MAX_WORKER_RESTART_INTERVAL = 20 * 60 * 1000L
        fun getIntervalBetweenWords() = (2..5).random() * 60 * 1000L
    }
}