package com.nagel.wordnotification.data.session.entities

import com.nagel.wordnotification.data.accounts.entities.Account


data class SessionDataEntity(
    var currentDictionaryId: Long? = null,
    var account: Account? = null,
    var dateAppInstallation: Long? = null,
    var ratedApp: Boolean? = false,
    var stepRatedApp: Int? = 0,
    var isNotificationCreated: Boolean? = false
)