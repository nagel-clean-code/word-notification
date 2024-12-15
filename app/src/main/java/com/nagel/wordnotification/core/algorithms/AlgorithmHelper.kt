package com.nagel.wordnotification.core.algorithms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.nagel.wordnotification.Constants.TAKE_AWAY
import com.nagel.wordnotification.Constants.dateFormat
import com.nagel.wordnotification.Constants.dayDateFormat
import com.nagel.wordnotification.app.App
import com.nagel.wordnotification.core.services.AlarmReceiver
import com.nagel.wordnotification.core.services.NotificationDto
import com.nagel.wordnotification.data.settings.entities.ModeSettingsDto
import java.util.Calendar
import java.util.Date

object AlgorithmHelper {

    private const val ONE_DAY_MLS = 24 * 60 * 60 * 1000L

    fun createAlarm(word: NotificationDto) {
        Log.d("CoroutineWorker:startAlarm:", word.toString())
        val appContext = App.get()
        val intent = Intent(appContext, AlarmReceiver::class.java)
        val json = Gson().toJson(word)
        intent.putExtra(TAKE_AWAY, json)
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            word.uniqueId + word.step,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        Log.d(
            "CoroutineWorker:startAlarm:",
            "requestCode: ${word.uniqueId + word.step}" + ", Name:${word.text}"
        )
        val alarmManager = ContextCompat.getSystemService(appContext, AlarmManager::class.java)
        alarmManager?.setExact(AlarmManager.RTC_WAKEUP, word.date, pendingIntent)
    }

    fun nextAvailableDate(lastTime: Long, mode: ModeSettingsDto): Long {
        var time = lastTime
        fun goBeginningDay() {
            val c = Calendar.getInstance()
            c.time = Date(time)
            val hours = c.get(Calendar.HOUR_OF_DAY)
            val minutes = c.get(Calendar.MINUTE)
            val seconds = c.get(Calendar.SECOND)
            time -= (hours * 60 * 60 * 1000 + minutes * 60 * 1000 + seconds * 1000)
        }
        if (mode.sampleDays) {
            var isChangedDay = false
            while (checkDays(mode, time).not()) {
                isChangedDay = true
                time += ONE_DAY_MLS
            }
            if (isChangedDay) goBeginningDay()
        }

        fun goNextDay(): Long {
            time += ONE_DAY_MLS
            goBeginningDay()
            return nextAvailableDate(time, mode)
        }
        if (mode.timeIntervals) {
            if (checkMinutes(mode, time).not()) {
                val c = Calendar.getInstance()
                c.time = Date(time)
                val hours = c.get(Calendar.HOUR_OF_DAY)
                val minutes = c.get(Calendar.MINUTE)
                val interval = mode.intervalsDto?.balancingTimeIntervals()
                interval?.apply {
                    if (hours > eIHour) {
                        return goNextDay()
                    } else if (hours == eIHour && minutes > eIMinutes) {
                        return goNextDay()
                    } else if (hours < sIHour) {
                        val subHoursWithMin = (sIHour - hours) * 60
                        time += (subHoursWithMin + (sIMinutes - minutes)) * 60 * 1000
                    } else if (hours == sIHour && minutes < sIMinutes) {
                        time += (sIMinutes - minutes) * 60 * 1000
                    }
                }
            }
        }
        return time
    }

    fun checkOccurrenceInTimeInterval(time: Long, mode: ModeSettingsDto): Boolean {
        val timeInterval = checkMinutes(mode, time)
        val daysSelected = checkDays(mode, time)
        return timeInterval && daysSelected
    }

    private fun checkDays(mode: ModeSettingsDto, time: Long): Boolean {
        if (mode.sampleDays) {
            val day = dayDateFormat.format(Date(time))
            val str = day[0].uppercase() + day[1]
            return mode.days.contains(str)
        }
        return true
    }

    private fun checkMinutes(mode: ModeSettingsDto, time: Long): Boolean {
        if (mode.timeIntervals.not()) return true
        var timeInterval = true
        mode.intervalsDto?.apply {
            balancingTimeIntervals()
            val c = Calendar.getInstance()
            c.time = Date(time)
            val hours = c.get(Calendar.HOUR_OF_DAY)
            val minutes = c.get(Calendar.MINUTE)

            if (sIHour < eIHour) {
                if (hours < sIHour || hours > eIHour) {
                    timeInterval = false
                } else if (hours == sIHour && minutes < sIMinutes) {
                    timeInterval = false
                } else if (hours == eIHour && minutes > eIMinutes) {
                    timeInterval = false
                }
            } else if (sIHour > eIHour) {
                if (hours > sIHour || hours < eIHour) {
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
                        "timeInterval = $timeInterval, "
            )
        }
        return timeInterval
    }
}