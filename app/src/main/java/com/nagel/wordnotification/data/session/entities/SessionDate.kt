package com.nagel.wordnotification.data.session.entities

import com.nagel.wordnotification.data.accounts.entities.Account


data class SessionDataEntity(
    val currentDictionaryId: Long? = null,
    var account: Account? = null
)