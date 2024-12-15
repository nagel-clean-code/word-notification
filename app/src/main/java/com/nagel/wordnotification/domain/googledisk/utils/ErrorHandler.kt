package com.nagel.wordnotification.domain.googledisk.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.nagel.wordnotification.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

interface ErrorHandler {

    fun handeError(exception: Throwable)

    fun getErrorMessage(exception: Throwable): String

    @Singleton
    class DefaultErrorHandler @Inject constructor(
        @ApplicationContext private val context: Context
    ) : ErrorHandler {
        override fun handeError(exception: Throwable) {
            Log.e(javaClass.simpleName, "Error!", exception)
            if (exception is AuthException) return
            Toast.makeText(context, getErrorMessage(exception), Toast.LENGTH_SHORT).show()
        }

        override fun getErrorMessage(exception: Throwable): String {
            return when (exception) {
                is CalledNotFromUiException -> context.getString(R.string.error_called_not_from_ui)
                is LoginFailedException -> context.getString(
                    R.string.error_login_failed,
                    exception.message
                )

                is AlreadyInProgressException -> context.getString(R.string.error_in_progress)
                is ConnectionException -> context.getString(R.string.error_connection)
                else -> context.getString(R.string.error_unknown)
            }
        }
    }
}

fun ErrorHandler.launchIn(
    scope: CoroutineScope,
    customErrorHandler: (Throwable) -> Boolean = { false },
    finally: () -> Unit = {},
    block: suspend () -> Unit,
) {
    scope.launch {
        try {
            block()
        } catch (e: Exception) {
            if (e !is OperationCancelledException && !customErrorHandler(e)) {
                handeError(e)
            }
        } finally {
            finally()
        }
    }
}

suspend fun suppressExceptions(block: suspend () -> Unit) {
    try {
        block()
    } catch (e: Throwable) {
    }
}