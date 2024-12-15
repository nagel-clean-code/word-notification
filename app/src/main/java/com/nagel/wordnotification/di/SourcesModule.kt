package com.nagel.wordnotification.di

import com.nagel.wordnotification.data.googledisk.accounts.AccountsSource
import com.nagel.wordnotification.data.googledisk.accounts.ActivityRequired
import com.nagel.wordnotification.data.googledisk.accounts.google.GoogleAccountsSource
import com.nagel.wordnotification.data.googledisk.files.FilesSource
import com.nagel.wordnotification.data.googledisk.files.google.GoogleFilesSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class SourcesModule {

    @Provides
    fun bindGoogleAccountsSourceAsSource(source: GoogleAccountsSource): AccountsSource {
        return source
    }

    @Provides
    fun bindGoogleAccountsSourceAsActivityRequired(source: GoogleAccountsSource): ActivityRequired {
        return source
    }

    @Provides
    fun bindFilesSource(source: GoogleFilesSource): FilesSource {
        return source
    }

}