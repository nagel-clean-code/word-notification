package com.nagel.wordnotification.di

import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.dictionaries.room.RoomDictionaryRepository
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.data.session.sharedprefs.SharedprefSessionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class BindDataModule {

    @Binds
    abstract fun bindSessionRepository(sharedPreferences: SharedprefSessionRepository): SessionRepository

    @Binds
    abstract fun bindDictionaryRepository(sharedPreferences: RoomDictionaryRepository): DictionaryRepository

//    @Binds
//    abstract fun provideStorage(): Storage

}