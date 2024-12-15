package com.nagel.wordnotification.di

import com.nagel.wordnotification.domain.googledisk.utils.ErrorHandler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface ErrorHandlerModule {

    @Binds
    fun bindDefaultErrorToStringMapper(
        errorHandler: ErrorHandler.DefaultErrorHandler
    ): ErrorHandler

}