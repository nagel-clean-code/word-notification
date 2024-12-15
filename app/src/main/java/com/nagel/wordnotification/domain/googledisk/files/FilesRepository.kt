package com.nagel.wordnotification.domain.googledisk.files

import com.nagel.wordnotification.data.googledisk.files.FilesSource
import com.nagel.wordnotification.domain.googledisk.utils.Result
import com.nagel.wordnotification.domain.googledisk.utils.ignoreErrors
import com.nagel.wordnotification.domain.googledisk.utils.suppressExceptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onSubscription
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilesRepository @Inject constructor(
    private val filesSource: FilesSource,
) {

    private val filesFlow = MutableStateFlow<Result<List<RemoteFile>>>(Result.Pending())

    fun getFiles(): Flow<Result<List<RemoteFile>>> {
        return filesFlow
            .onSubscription {
                emit(Result.Pending())
                ignoreErrors { reload(silently = true) }
            }
    }

    suspend fun reload(silently: Boolean = false) {
        try {
            if (!silently) filesFlow.value = Result.Pending()
            filesFlow.value = Result.Success(filesSource.getFiles())
        } catch (e: Exception) {
            filesFlow.value = Result.Error(e)
            throw e
        }
    }

    suspend fun delete(file: RemoteFile) {
        filesSource.delete(file)
        suppressExceptions {
            reload(silently = true)
        }
    }

    suspend fun upload(file: File) {
        try {
            filesSource.upload(file)
            suppressExceptions {
                reload(silently = true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}