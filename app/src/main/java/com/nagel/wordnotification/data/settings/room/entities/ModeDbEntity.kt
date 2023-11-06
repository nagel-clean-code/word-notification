package com.nagel.wordnotification.data.settings.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.nagel.wordnotification.data.dictionaries.room.entities.DictionaryDbEntity
import com.nagel.wordnotification.data.settings.entities.ModeSettingsDto
import com.nagel.wordnotification.data.settings.entities.SelectedMode


@Entity(
    tableName = "modes",
    foreignKeys = [
        ForeignKey(
            entity = DictionaryDbEntity::class,
            parentColumns = ["id"],
            childColumns = ["id_dictionary"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
class ModeDbEntity(
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) var idMode: Long,
    @ColumnInfo(name = "id_dictionary") val idDictionary: Long,
    @ColumnInfo(name = "selected_mode") val selectedMode: String,
    @ColumnInfo(name = "sample_days") val sampleDays: Boolean,
    @ColumnInfo(name = "time_intervals") val timeIntervals: Boolean,
    @ColumnInfo(name = "days_in_json") val daysInJson: String,
    @ColumnInfo(name = "time_intervals_first") val timeIntervalsFirst: String,
    @ColumnInfo(name = "time_intervals_second") val timeIntervalsSecond: String,
) {

    fun toMode(): ModeSettingsDto {
        return ModeSettingsDto(
            idMode = idMode,
            idDictionary = idDictionary,
            selectedMode = getSelectedMode(),
            sampleDays = sampleDays,
            days = getDaysInListFromJson(),
            timeIntervals = timeIntervals,
            workingTimeInterval = Pair(timeIntervalsFirst, timeIntervalsSecond)
        )
    }

    private fun getDaysInListFromJson(): List<String> {
        return Gson().fromJson(daysInJson, Array<String>::class.java).toList()
    }

    private fun getSelectedMode(): SelectedMode? {
        return when (selectedMode) {
            SelectedMode.PlateauEffect::class.simpleName -> {
                SelectedMode.PlateauEffect
            }

            SelectedMode.ForgetfulnessCurveLong::class.simpleName -> {
                SelectedMode.ForgetfulnessCurveLong
            }

            SelectedMode.ForgetfulnessCurve::class.simpleName -> {
                SelectedMode.ForgetfulnessCurve
            }

            else -> {
                null
            }
        }
    }

    companion object {
        fun createMode(mode: ModeSettingsDto) = ModeDbEntity(
            idMode = 0,
            idDictionary = mode.idDictionary,
            selectedMode = mode.selectedMode?.let { it::class.simpleName.toString() } ?: "",
            sampleDays = mode.sampleDays,
            daysInJson = mode.getDaysInJson(),
            timeIntervals = mode.timeIntervals,
            timeIntervalsFirst = mode.workingTimeInterval.first,
            timeIntervalsSecond = mode.workingTimeInterval.second,
        )
    }
}