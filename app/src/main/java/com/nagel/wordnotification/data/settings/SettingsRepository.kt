package com.nagel.wordnotification.data.settings

import com.nagel.wordnotification.data.settings.entities.ModeSettingsDto
import com.nagel.wordnotification.data.settings.room.entities.ModeDbEntity

interface SettingsRepository {
    suspend fun saveModeSettings(data: ModeSettingsDto)
    suspend fun getModeSettingsById(idMode: Long): ModeDbEntity?
    suspend fun getModes(): List<ModeSettingsDto>?
}