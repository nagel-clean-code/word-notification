package com.nagel.wordnotification.data.room

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.nagel.wordnotification.data.accounts.room.AccountDao
import com.nagel.wordnotification.data.accounts.room.entities.AccountDbEntity
import com.nagel.wordnotification.data.dictionaries.room.DictionaryDao
import com.nagel.wordnotification.data.dictionaries.room.entities.DictionaryDbEntity
import com.nagel.wordnotification.data.dictionaries.room.entities.NotificationHistoryDbEntity
import com.nagel.wordnotification.data.dictionaries.room.entities.WordDbEntity
import com.nagel.wordnotification.data.settings.room.ModeDao
import com.nagel.wordnotification.data.settings.room.entities.ModeDbEntity

@Database(
    version = 2,
    entities = [
        DictionaryDbEntity::class,
        AccountDbEntity::class,
        WordDbEntity::class,
        ModeDbEntity::class,
        NotificationHistoryDbEntity::class
    ],
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
        //,AutoMigration (from = 2, to = 3)
    ]
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getAccountsDao(): AccountDao
    abstract fun getDictionaryDao(): DictionaryDao
    abstract fun getModeDao(): ModeDao
}