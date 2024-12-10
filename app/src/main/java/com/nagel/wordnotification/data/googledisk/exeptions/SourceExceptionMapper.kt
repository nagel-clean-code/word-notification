package com.nagel.wordnotification.data.googledisk.exeptions

import com.nagel.wordnotification.domain.googledisk.utils.AppException

interface SourceExceptionMapper {

    fun mapExceptions(exception: Throwable): AppException

    suspend fun <T> wrap(block: suspend () -> T): T {
        try {
            return block()
        } catch (e: Throwable) {
            if (e is AppException) {
                throw e
            } else {
                throw mapExceptions(e)
            }
        }
    }

}