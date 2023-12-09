package com.nagel.wordnotification.utils.common

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun String.toDateLong(formatFrom: String): Long? {
    try {
        val sdf = SimpleDateFormat(formatFrom, Locale.getDefault()).also {
            it.isLenient = false
            val timeZoneId = "UTC"
            it.timeZone = TimeZone.getTimeZone(timeZoneId)
        }
        return sdf.parse(this)?.time
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

fun String?.replaceFirstCharLowercase(): String? {
    return this?.replaceFirstChar { it.lowercase(Locale.getDefault()) }
}

fun String?.toSentence(): String? {
    return this?.lowercase()?.replaceFirstChar { it.titlecase(Locale.getDefault()) }
}

fun String?.toNotNullSentence(): String {
    return this?.lowercase()?.replaceFirstChar { it.titlecase(Locale.getDefault()) } ?: String()
}

/**
 * Форматирует текст в соответствии с маской
 * @param mask маска для применения, например "X XXX XX"
 * @param maskChar символ маски, в примере выше 'X'
 */
@Throws(IllegalArgumentException::class)
fun String.applyMask(
    mask: String,
    maskChar: Char = 'X'
): String {
    val stringBuilder = StringBuilder()
    val textCharsIterator = iterator()
    loop@ for (char in mask) {
        if (char == maskChar) {
            val textChar = if (textCharsIterator.hasNext()) {
                textCharsIterator.next()
            } else break@loop
            stringBuilder.append(textChar)
        } else {
            stringBuilder.append(char)
        }
    }
    return stringBuilder.toString().trimEnd()
}

fun String?.removeDuplicateSpaces(): String? {
    return this?.trim()?.replace("\\s+".toRegex(), " ")
}

fun String?.isNotNullOrBlank() = this.isNullOrBlank().not()