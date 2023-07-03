package com.nagel.wordnotification.data.accounts

import com.nagel.wordnotification.data.accounts.entities.Account

interface AccountRepository {

    suspend fun saveAccount(account: Account)
}