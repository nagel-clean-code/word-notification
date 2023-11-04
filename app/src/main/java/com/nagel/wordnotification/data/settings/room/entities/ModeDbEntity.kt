package com.nagel.wordnotification.data.settings.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
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
    @ColumnInfo(name = "time_intervals_first") val timeIntervalsFirst: String,
    @ColumnInfo(name = "time_intervals_second") val timeIntervalsSecond: String,
) {

    fun toMode(): ModeSettingsDto {
        return ModeSettingsDto(
            idMode = idMode,
            idDictionary = idDictionary,
            selectedMode = getSelectedMode(),
            sampleDays = sampleDays,
            days = listOf(),    //Получать из SharedPrefs
            timeIntervals = timeIntervals,
            workingTimeInterval = Pair(timeIntervalsFirst, timeIntervalsSecond)
        )
    }

    private fun getSelectedMode(): SelectedMode {
        return when (selectedMode) {
            SelectedMode.PlateauEffect.toString() -> {
                SelectedMode.PlateauEffect
            }

            SelectedMode.ForgetfulnessCurveLong.toString() -> {
                SelectedMode.ForgetfulnessCurveLong
            }

            SelectedMode.ForgetfulnessCurve.toString() -> {
                SelectedMode.ForgetfulnessCurve
            }

            else -> SelectedMode.ForgetfulnessCurve
        }
    }

    companion object {
        fun createMode(mode: ModeSettingsDto) = ModeDbEntity(
            idMode = 0,
            idDictionary = mode.idDictionary,
            selectedMode = mode.selectedMode.toString(),
            sampleDays = mode.sampleDays,
            timeIntervals = mode.timeIntervals,
            timeIntervalsFirst = mode.workingTimeInterval.first,
            timeIntervalsSecond = mode.workingTimeInterval.second,
        )
    }
}