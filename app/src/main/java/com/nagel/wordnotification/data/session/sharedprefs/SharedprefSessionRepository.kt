package com.nagel.wordnotification.data.session.sharedprefs

import android.content.Context
import com.google.gson.Gson
import com.nagel.wordnotification.data.accounts.entities.Account
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.data.session.entities.SessionDataEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SharedprefSessionRepository @Inject constructor(
    @ApplicationContext val context: Context
) : SessionRepository {

    private val sharedPreferences =
        context.getSharedPreferences(SHARED_PREFS_SESSION, Context.MODE_PRIVATE)

    override fun saveAccount(account: Account) {
        val currentSession = getSession()
        currentSession.account = account
        currentSession.let { saveSession(it) }
    }

    override fun saveSession(data: SessionDataEntity) {
        val json = Gson().toJson(data)
        sharedPreferences.edit().putString(SESSiON_STATE, json).apply()
    }

    override fun saveCurrentIdDictionary(idDictionary: Long) {
        val currentSession = getSession()
        currentSession.currentDictionaryId = idDictionary
        saveSession(currentSession)
    }

    override fun getSession(): SessionDataEntity {
        val json = sharedPreferences.getString(SESSiON_STATE, "")
        return if (json?.isBlank() == true) {
            createSession()
        } else {
            Gson().fromJson(json, SessionDataEntity::class.java)
        }
    }

    override fun getAccountId(): Long? = getSession().account?.id

    override fun getPreviewFlag(screenCode: String): Boolean {
        val permissionShow = sharedPreferences.getBoolean(screenCode, true)
        if (!permissionShow) return false
        sharedPreferences.edit().putBoolean(screenCode, false).apply()
        return true
    }

    override fun getTranslationLanguage(): String {
        return sharedPreferences.getString(TRANSLATION_LANGUAGE, "en") ?: "en"
    }

    override fun saveTranslationLanguage(lang: String) {
        sharedPreferences.edit().putString(TRANSLATION_LANGUAGE, lang).apply()
    }

    override fun getAutoTranslation(): Boolean {
        return sharedPreferences.getBoolean(AUTO_TRANSLATION, true)
    }

    override fun saveAutoTranslation(isAuto: Boolean) {
        sharedPreferences.edit().putBoolean(AUTO_TRANSLATION, isAuto).apply()
    }

    private fun createSession(): SessionDataEntity {
        val currentTime = Date().time
        val loadSession = SessionDataEntity(dateAppInstallation = currentTime)
        return loadSession.apply {
            dateAppInstallation ?: run {
                dateAppInstallation = currentTime
                ratedApp = false
                stepRatedApp = 0
                saveSession(this@apply)
            }
        }
    }

    companion object {
        private const val TRANSLATION_LANGUAGE = "TRANSLATION_LANGUAGE"
        private const val AUTO_TRANSLATION = "AUTO_TRANSLATION"
        private const val SESSiON_STATE = "SESSiON_STATE"
        private const val SHARED_PREFS_SESSION = "SHARED_PREFS_SESSION"
    }
}