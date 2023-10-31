package com.nagel.wordnotification.di

import android.content.Context
import androidx.room.Room
import com.nagel.wordnotification.data.accounts.room.AccountDao
import com.nagel.wordnotification.data.dictionaries.room.DictionaryDao
import com.nagel.wordnotification.data.room.AppDatabase
import com.nagel.wordnotification.data.settings.room.ModeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ProvideDataModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DB_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideDictionaryDao(database: AppDatabase): DictionaryDao {
        return database.getDictionaryDao()
    }

    @Provides
    @Singleton
    fun provideModeDao(database: AppDatabase): ModeDao {
        return database.getModeDao()
    }

    @Provides
    @Singleton
    fun provideAccountDao(database: AppDatabase): AccountDao {
        return database.getAccountsDao()
    }

    private companion object {
        const val DB_NAME = "recipes.db"
    }
}