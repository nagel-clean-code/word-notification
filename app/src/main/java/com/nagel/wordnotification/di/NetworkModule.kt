package com.nagel.wordnotification.di

import com.google.gson.Gson
import com.nagel.wordnotification.data.googledisk.exeptions.DefaultSourceExceptionMapper
import com.nagel.wordnotification.data.googledisk.exeptions.SourceExceptionMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideExceptionMapper(): SourceExceptionMapper {
        return DefaultSourceExceptionMapper()
    }

}