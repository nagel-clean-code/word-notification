package com.nagel.wordnotification.data.settings.entities

data class ModeSettingsDto(
    val idMode: Long = 0,
    val idDictionary: Long,
    val selectedMode: SelectedMode,
    val sampleDays: Boolean,
    val days: List<String>,
    val timeIntervals: Boolean,
    val workingTimeInterval: Pair<String, String>,

//    val repeat: Pair<String, String>,
//    val repeatWords: Boolean,
//    val repeatCount: Int
)

sealed class SelectedMode {
    object PlateauEffect : SelectedMode()
    object ForgetfulnessCurveLong : SelectedMode()
    object ForgetfulnessCurve : SelectedMode()
}
