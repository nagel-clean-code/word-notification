package com.nagel.wordnotification.data.accounts.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nagel.wordnotification.Constants
import com.nagel.wordnotification.data.accounts.entities.Account
import java.util.UUID


@Entity(
    tableName = "accounts",
    indices = [
        Index("id_author", unique = true)
    ]
)
class AccountDbEntity(
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val idAccount: Long,
    @ColumnInfo(name = "id_author") val idAuthorUUID: String,
    val name: String
) {

    fun toAccount(): Account {
        return Account(
            id = idAccount,
            idAuthorUUID = idAuthorUUID,
            userName = name
        )
    }

    companion object {

        fun createAccount(name: String = Constants.DEFAULT_USER_NAME) = AccountDbEntity(
            idAccount = 0,
            idAuthorUUID = UUID.randomUUID().toString(),
            name = name
        )
    }
}


