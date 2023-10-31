package com.nagel.wordnotification.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagel.wordnotification.data.accounts.entities.Account
import com.nagel.wordnotification.data.accounts.room.AccountDao
import com.nagel.wordnotification.data.accounts.room.entities.AccountDbEntity
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.dictionaries.room.DictionaryDao
import com.nagel.wordnotification.data.session.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
//            delay(100)  //TODO проверить
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

}