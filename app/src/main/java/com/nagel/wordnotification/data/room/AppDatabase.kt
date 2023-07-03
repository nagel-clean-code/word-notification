package com.nagel.wordnotification.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nagel.wordnotification.data.accounts.room.AccountDao
import com.nagel.wordnotification.data.accounts.room.entities.AccountDbEntity
import com.nagel.wordnotification.data.dictionaries.room.DictionaryDao
import com.nagel.wordnotification.data.dictionaries.room.entities.DictionaryDbEntity
import com.nagel.wordnotification.data.dictionaries.room.entities.WordDbEntity

@Database(
    version = 1,
    entities = [
        DictionaryDbEntity::class,
        AccountDbEntity::class,
        WordDbEntity::class,
    ]
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getAccountsDao(): AccountDao

    abstract fun getDictionaryDao(): DictionaryDao

}