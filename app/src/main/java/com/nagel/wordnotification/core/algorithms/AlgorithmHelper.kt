package com.nagel.wordnotification.core.algorithms

import android.util.Log
import com.nagel.wordnotification.data.settings.entities.ModeSettingsDto
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

object AlgorithmHelper {

    fun nextAvailableDate(lastTime: Long, mode: ModeSettingsDto): Long {
        var countRepeater = 0
        var currentTime = lastTime
        while (!checkOccurrenceInTimeInterval(currentTime, mode)) {
            currentTime += 15 * 60 * 1000
            if (countRepeater++ > 100000) {
                return currentTime
            }
        }
        return currentTime
    }

    fun checkOccurrenceInTimeInterval(time: Long, mode: ModeSettingsDto): Boolean {
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
        var timeInterval = true
        mode.intervalsDto?.apply {
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
            val dateTime = NotificationAlgorithm.dateFormat.format(Date(time))
            Log.d(
                "CoroutineWorker:",
                "CHECK_TIME: time = $dateTime, " +
                        "timeInterval = $timeInterval, "
            )
        }
        return timeInterval
    }
}