package com.nagel.wordnotification.utils.common

import android.util.Log

object ExceptionUtils {
    fun <R> tryOrNull(body: () -> R): R? {
        return try {
            body()
        } catch (e: Throwable) {
            Log.e("e",e.stackTraceToString())
            null
        }
    }
}
