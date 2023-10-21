package com.nagel.wordnotification.data.settings.sharedprefs

import android.content.Context
import com.google.gson.Gson
import com.nagel.wordnotification.data.session.entities.SessionDataEntity
import com.nagel.wordnotification.data.settings.SettingsRepository
import com.nagel.wordnotification.data.settings.entities.ModeSettingsDto
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SharedprefSettingsRepository @Inject constructor(
    @ApplicationContext val context: Context
) : SettingsRepository {

    private val sharedPreferences =
        context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE)

    private var modeSettingsDto: ModeSettingsDto? = null


    override fun saveModeSettings(data: ModeSettingsDto) {
        val json = Gson().toJson(data)
        sharedPreferences.edit().putString(MODE_SETTINGS, json).apply()
        modeSettingsDto = data
    }

    override fun getModeSettings(): ModeSettingsDto? {
        if (modeSettingsDto != null)
            return modeSettingsDto

        val json = sharedPreferences.getString(MODE_SETTINGS, "")
        modeSettingsDto = if (json?.isBlank() == true) {
            null
        } else {
            Gson().fromJson(json, ModeSettingsDto::class.java)
        }
        return modeSettingsDto
    }

    companion object {
        private const val MODE_SETTINGS = "MODE_SETTINGS"
        private const val SETTINGS = "SETTINGS"
    }
}