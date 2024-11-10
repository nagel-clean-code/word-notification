package com.nagel.wordnotification.data.settings.entities

import com.google.gson.Gson
import com.nagel.wordnotification.core.algorithms.Algorithm

data class ModeSettingsDto(
    var idMode: Long = 0,
    val idDictionary: Long,
    val selectedMode: Algorithm?,
    val sampleDays: Boolean,
    val days: List<String>,
    val timeIntervals: Boolean,
    val workingTimeInterval: Pair<String, String>,
) {
    var intervalsDto: IntervalsDto? = null
        get() {
            if (field == null) {
                field = buildIntervalsDto()
            }
            return field
        }

    fun getDaysInJson(): String {
        return Gson().toJson(days)
    }

    private fun buildIntervalsDto(): IntervalsDto {
        val startInterval = workingTimeInterval.first
        val endInterval = workingTimeInterval.second

        val sIHour = startInterval.substring(0, startInterval.indexOf(':')).toInt()
        val sIMinutes = startInterval.substring(startInterval.indexOf(':') + 1).toInt()

        val eIHour = endInterval.substring(0, endInterval.indexOf(':')).toInt()
        val eIMinutes = endInterval.substring(endInterval.indexOf(':') + 1).toInt()
        return IntervalsDto(sIHour, sIMinutes, eIHour, eIMinutes)
    }

}

data class IntervalsDto(val sIHour: Int, val sIMinutes: Int, val eIHour: Int, val eIMinutes: Int) {

    //TODO нужно перенести эту балансировку при сохранении интервала
    fun balancingTimeIntervals(): IntervalsDto {
        var newSIHour = sIHour
        var newSIMinutes = sIMinutes
        var newEIHour = eIHour
        var newEIMinutes = eIMinutes

        fun swap() {
            newSIHour = eIHour
            newEIHour = sIHour
            newSIMinutes = eIMinutes
            newEIMinutes = sIMinutes
        }

        if (sIHour > eIHour) swap()
        if (sIHour == eIHour && sIMinutes > eIMinutes) swap()

        return this.copy(
            sIHour = newSIHour,
            sIMinutes = newSIMinutes,
            eIHour = newEIHour,
            eIMinutes = newEIMinutes
        )
    }
}