package com.nagel.wordnotification.data.settings.entities

import com.google.gson.Gson
import com.nagel.wordnotification.core.algorithms.Algorithm

data class ModeSettingsDto(
    val idMode: Long = 0,
    val idDictionary: Long,
    val selectedMode: Algorithm?,
    val sampleDays: Boolean,
    val days: List<String>,
    val timeIntervals: Boolean,
    val workingTimeInterval: Pair<String, String>,
) {
    fun getDaysInJson(): String {
        return Gson().toJson(days)
    }
}