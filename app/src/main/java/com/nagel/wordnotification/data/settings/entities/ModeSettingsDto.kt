package com.nagel.wordnotification.data.settings.entities

data class ModeSettingsDto(
    val idDictionary: Long,
    val forgetfulnessCurve: Boolean,
    val sampleDays: Boolean,
    val days: List<String>,
    val timeIntervals: Boolean,
    val workingTimeInterval: Pair<String, String>,
    val repeat: Pair<String, String>,
    val repeatWords: Boolean,
    val repeatCount: Int
)