package com.nagel.wordnotification.presentation.profile

import com.nagel.wordnotification.data.firbase.entity.CurrentPrices
import com.nagel.wordnotification.utils.common.Event

data class ProfileUiState(
    val isStarted: Boolean = false,
    val premiumIsDisabledEvent: Event<Unit>? = null,
    val showPremiumEvent: Event<Unit>? = null,
    val showErrorEvent: Event<Unit>? = null,
    val chowPremiumInformationEvent: Event<CurrentPrices>? = null,
    val youHavePremium: Event<String>? = null
)