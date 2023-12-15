package com.nagel.wordnotification.presentation

import androidx.lifecycle.ViewModel
import com.nagel.wordnotification.data.session.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class MainActivityVM @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    fun isItPossibleShowRateApp(): Boolean {
        val session = sessionRepository.getSession()
        val step = session.stepRatedApp ?: return false
        val date = session.dateAppInstallation ?: return false
        val nextInterval = mapSteps[step] ?: return false
        return Date().time - date > nextInterval
    }

    companion object {
        private const val BEGINNING_OF_SHOW = 3 * 24 * 60 * 60 * 1000L

        private val mapSteps = mapOf(
            0 to BEGINNING_OF_SHOW,             //3 дня
            1 to BEGINNING_OF_SHOW * 4,         //12 дней
            2 to BEGINNING_OF_SHOW * 10,        //1 месяца
            3 to BEGINNING_OF_SHOW * 10 * 2,    //2 месяца
            4 to BEGINNING_OF_SHOW * 10 * 4,    //4 месяца
            5 to BEGINNING_OF_SHOW * 10 * 12,   //1 год
        )
    }
}