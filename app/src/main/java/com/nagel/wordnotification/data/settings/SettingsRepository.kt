package com.nagel.wordnotification.data.settings

import com.nagel.wordnotification.data.settings.entities.ModeSettingsDto

interface SettingsRepository {
    fun saveModeSettings(data: ModeSettingsDto)
    fun getModeSettings(): ModeSettingsDto?
}