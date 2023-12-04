package com.nagel.wordnotification.presentation.addingwords.worddetails.widget.model

import com.nagel.wordnotification.data.settings.entities.ModeSettingsDto

data class ShowStepsWordDto(
    val mode: ModeSettingsDto,
    var allNotificationsCreated: Boolean = false,
    var learnStep: Int = 0,
    var lastDateMention: Long = 0,
)
