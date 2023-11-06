package com.nagel.wordnotification.data.settings.entities

import com.google.gson.Gson

data class ModeSettingsDto(
    val idMode: Long = 0,
    val idDictionary: Long,
    val selectedMode: SelectedMode?,
    val sampleDays: Boolean,
    val days: List<String>,
    val timeIntervals: Boolean,
    val workingTimeInterval: Pair<String, String>,
) {
    fun getDaysInJson(): String {
        return Gson().toJson(days)
    }
}

sealed class SelectedMode {
    object PlateauEffect : SelectedMode()
    object ForgetfulnessCurveLong : SelectedMode()
    object ForgetfulnessCurve : SelectedMode()
}
