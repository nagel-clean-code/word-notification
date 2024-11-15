package com.nagel.wordnotification.data.session

import com.nagel.wordnotification.data.accounts.entities.Account
import com.nagel.wordnotification.data.session.entities.SessionDataEntity

interface SessionRepository {
    fun saveAccount(account: Account)
    fun saveSession(data: SessionDataEntity)
    fun saveCurrentIdDictionary(idDictionary: Long)
    fun getSession(): SessionDataEntity
    fun updateIsNotificationCreated(isNotificationCreated: Boolean)
    fun getAccountId(): Long?
    fun getPreviewFlag(screenCode: String): Boolean
    fun getTranslationLanguage(): String
    fun saveTranslationLanguage(lang: String)
    fun getWordLanguage(): String
    fun saveWordLanguage(lang: String)
    fun getAutoTranslation(): Boolean
    fun saveAutoTranslation(isAuto: Boolean)
    fun saveCurrentWordNotification(idWord: Long)
    fun getCurrentWordIdNotification(): Long
}