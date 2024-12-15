package com.nagel.wordnotification.domain.googledisk.utils

open class AppException(
    message: String = "",
    cause: Throwable? = null
) : Exception(message, cause)

class AuthException : AppException()

class CalledNotFromUiException : AppException()

class AlreadyInProgressException : AppException()

class LoginFailedException(
    message: String,
    cause: Throwable?
) : AppException(message, cause)

class InternalException(
    cause: Throwable?
) : AppException(cause = cause)

class ReadFileException(
    cause: Throwable?
) : AppException(cause = cause)

class OperationCancelledException() : AppException()

class ConnectionException(
    cause: Throwable?
) : AppException(cause = cause)

suspend fun ignoreErrors(block: suspend () -> Unit) {
    try {
        block()
    } catch (e: Throwable) {
    }
}