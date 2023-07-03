package com.nagel.wordnotification.data.session

import com.nagel.wordnotification.data.accounts.entities.Account
import com.nagel.wordnotification.data.session.entities.SessionDataEntity

interface SessionRepository {
    suspend fun saveAccount(account: Account)
    suspend fun saveSession(data: SessionDataEntity)
    suspend fun getSession(): SessionDataEntity?
}