package com.nagel.wordnotification.data.session

import com.nagel.wordnotification.data.accounts.entities.Account
import com.nagel.wordnotification.data.session.entities.SessionDataEntity

interface SessionRepository {
    fun saveAccount(account: Account)
    fun saveSession(data: SessionDataEntity)
    fun saveCurrentIdDictionary(idDictionary: Long)
    fun getSession(): SessionDataEntity
    fun getAccountId(): Long?
}