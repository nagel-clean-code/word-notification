package com.nagel.wordnotification.data.googledisk.exeptions

import com.nagel.wordnotification.domain.googledisk.utils.AppException
import com.nagel.wordnotification.domain.googledisk.utils.ConnectionException
import com.nagel.wordnotification.domain.googledisk.utils.InternalException
import java.io.IOException

class DefaultSourceExceptionMapper : SourceExceptionMapper {

    override fun mapExceptions(exception: Throwable): AppException {
        when (exception) {
            is IOException -> return ConnectionException(exception)
            else -> return InternalException(exception)
        }
    }

}