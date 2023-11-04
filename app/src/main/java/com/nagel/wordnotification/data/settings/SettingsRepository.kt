package com.nagel.wordnotification.data.settings

import com.nagel.wordnotification.data.settings.entities.ModeSettingsDto
import com.nagel.wordnotification.data.settings.room.entities.ModeDbEntity
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    suspend fun saveModeSettings(data: ModeSettingsDto)
    suspend fun getModeSettings(idDictionary: Long): ModeDbEntity?
}