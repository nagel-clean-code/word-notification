package com.nagel.wordnotification.presentation.profile

import com.nagel.wordnotification.Constants.datePremium
import com.nagel.wordnotification.R
import com.nagel.wordnotification.data.firbase.RemoteDbRepository
import com.nagel.wordnotification.data.premium.PremiumRepository
import com.nagel.wordnotification.presentation.base.BaseViewModel
import com.nagel.wordnotification.presentation.navigator.NavigatorV2
import com.nagel.wordnotification.utils.Toggles
import com.nagel.wordnotification.utils.common.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ProfileVM @Inject constructor(
    private val remoteDbRepository: RemoteDbRepository,
    premiumRepository: PremiumRepository,
    navigatorV2: NavigatorV2
) : BaseViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state

    init {
        val isStarted = premiumRepository.getIsStarted()
        if (isStarted) {
            val dateEnd = premiumRepository.getDatePremiumEnd()
            val date = datePremium.format(dateEnd)
            var text = navigatorV2.getString(R.string.until_the)
            text = String.format(text, date)
            _state.value = _state.value.copy(youHavePremium = Event(text))
        } else {
            remoteDbRepository.getFeatureToggles(
                success = {
                    if (it.content.contains(Toggles.Purchases.name)) {
                        _state.value = _state.value.copy(showPremiumEvent = Event(Unit))
                    } else {
                        _state.value = _state.value.copy(premiumIsDisabledEvent = Event(Unit))
                    }
                },
                error = {
                    _state.value = _state.value.copy(showErrorEvent = Event(Unit))
                }
            )
        }
    }

    fun requestPremiumInformation() {
        remoteDbRepository.requestPremiumInformation(
            success = { result ->
                _state.value = _state.value.copy(chowPremiumInformationEvent = Event(result))
            },
            error = {
                _state.value = _state.value.copy(showErrorEvent = Event(Unit))
            }
        )
    }

}