package com.nagel.wordnotification.data.accounts.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.nagel.wordnotification.data.accounts.room.entities.AccountDbEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Insert(entity = AccountDbEntity::class)
    suspend fun addAccount(accountDbEntity: AccountDbEntity): Long

    @Query("SELECT * FROM accounts WHERE id = :accountId")
    fun getAccountById(accountId: Long): AccountDbEntity?

}