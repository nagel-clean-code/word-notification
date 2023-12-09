package com.nagel.wordnotification.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagel.wordnotification.data.accounts.entities.Account
import com.nagel.wordnotification.data.accounts.room.AccountDao
import com.nagel.wordnotification.data.accounts.room.entities.AccountDbEntity
import com.nagel.wordnotification.data.session.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class MainActivityVM @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val accountDao: AccountDao,
) : ViewModel() {

    private val _myAccountDbEntity = MutableStateFlow<Account?>(null)
    val myAccountDbEntity: StateFlow<Account?> = _myAccountDbEntity

    fun startSession() {
        viewModelScope.launch(Dispatchers.IO) {
            val session = sessionRepository.getSession()
            if (session == null) {
                setupNewUser()
            } else {
                val account = accountDao.getAccountById(session.account!!.id)
                _myAccountDbEntity.value = account?.toAccount()
            }
        }
    }

    private suspend fun setupNewUser() {
        val account = saveAccountInDb()
        sessionRepository.saveAccount(account)
        withContext(Dispatchers.Main) {
            _myAccountDbEntity.value = account
        }
    }

    private suspend fun saveAccountInDb(): Account {
        val accountDbEntity = AccountDbEntity.createAccount()
        val id = accountDao.addAccount(accountDbEntity)
        val account = accountDbEntity.toAccount()
        account.id = id
        return account
    }

    fun isItPossibleShowRateApp(): Boolean {
        val session = sessionRepository.getSession()
        val step = session?.stepRatedApp ?: return false
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