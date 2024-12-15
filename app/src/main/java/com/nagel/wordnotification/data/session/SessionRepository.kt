package com.nagel.wordnotification.data.session

import com.nagel.wordnotification.data.accounts.entities.Account
import com.nagel.wordnotification.data.session.entities.SessionDataEntity

interface SessionRepository {
    fun saveAccount(account: Account)
    fun getAccountId(): Long?

    fun saveSession(data: SessionDataEntity)
    fun getSession(): SessionDataEntity

    fun saveCurrentIdDictionary(idDictionary: Long)
    fun getPreviewFlag(screenCode: String): Boolean

    fun getTranslationLanguage(): String
    fun saveTranslationLanguage(lang: String)

    fun getAutoTranslation(): Boolean
    fun saveAutoTranslation(isAuto: Boolean)

    fun saveCurrentWordNotification(idWord: Long)
    fun getCurrentWordIdNotification(): Long

    fun saveIsAutoBackup(isAutoBackup: Boolean)
    fun getIsAutoBackupAndMark(): Boolean
    fun getIsAutoBackup(): Boolean

}