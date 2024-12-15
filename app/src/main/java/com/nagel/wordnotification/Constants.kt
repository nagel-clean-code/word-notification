package com.nagel.wordnotification

import java.text.SimpleDateFormat
import java.util.Locale

object Constants {
    const val DEFAULT_USER_NAME = "NO_NAME"
    const val TYPE = "TYPE"
    const val TYPE_ANSWER = 0
    const val TAKE_AWAY = "TAKE_AWAY"
    const val TYPE_QUEST = 1
    const val NOTIFICATION_CHANNEL_ID = "NotiFire_chanel_01"

    const val BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED"
    const val QUICKBOOT_POWERON = "android.intent.action.QUICKBOOT_POWERON"
    const val HTC_QUICKBOOT_POWERON = "com.htc.intent.action.QUICKBOOT_POWERON"

    const val NUMBER_OF_FREE_WORDS = 25
    const val COUNT_FREE_USE_RANDOMIZER = 5
    const val NUMBER_OF_FREE_WORDS_PER_ADVERTISEMENT = 5

    const val DICTIONARY_NAME = "dictionaries"
    const val FILE_FORMAT_FIRE = ".fire"
    const val DICTIONARY_NAME_WITH_FORMAT = DICTIONARY_NAME + FILE_FORMAT_FIRE

    //GOOGLE DISK
    const val GOOGLE_SIGN_IN_ACCOUNT = "GOOGLE_SIGN_IN_ACCOUNT"

    val simpleCurrentDateFormat by lazy { SimpleDateFormat("yyyy-MM-dd") }
    val dateFormat by lazy { SimpleDateFormat("d, HH:mm:ss") }
    val dayDateFormat by lazy { SimpleDateFormat("EE") }
    val dateTemplate by lazy { SimpleDateFormat("(yyyy.MM.dd, HH:mm:ss)", Locale.getDefault()) }
    val dateTemplateV2 by lazy { SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault()) }

    val datePremium by lazy { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }
}