package com.nagel.wordnotification.data.accounts.room

import com.nagel.wordnotification.data.accounts.AccountRepository
import com.nagel.wordnotification.data.accounts.entities.Account
import kotlinx.coroutines.CoroutineDispatcher

class RoomAccountRepository(
    private val ioDispatcher: CoroutineDispatcher
): AccountRepository {

    override suspend fun saveAccount(account: Account) {
        TODO("Not yet implemented")
    }

}