package com.nagel.wordnotification.presentation.exportdictionaries

import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.nagel.wordnotification.R
import com.nagel.wordnotification.data.dictionaries.DictionaryRepository
import com.nagel.wordnotification.data.premium.PremiumRepository
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.domain.googledisk.accounts.AccountsRepository
import com.nagel.wordnotification.domain.googledisk.files.FilesRepository
import com.nagel.wordnotification.domain.googledisk.utils.AuthException
import com.nagel.wordnotification.domain.googledisk.utils.ErrorHandler
import com.nagel.wordnotification.domain.googledisk.utils.Result
import com.nagel.wordnotification.domain.googledisk.utils.launchIn
import com.nagel.wordnotification.presentation.base.BaseViewModel
import com.nagel.wordnotification.presentation.exportAndImport.ExportGenerator
import com.nagel.wordnotification.presentation.navigator.NavigatorV2
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ExportVM @Inject constructor(
    private val exportGenerator: ExportGenerator,
    private val dictionaryRepository: DictionaryRepository,
    private val sessionRepository: SessionRepository,
    private val accountsRepository: AccountsRepository,
    private val errorHandler: ErrorHandler,
    private val filesRepository: FilesRepository,
    private val navigator: NavigatorV2,
    premiumRepository: PremiumRepository
) : BaseViewModel() {

    private val _state = MutableStateFlow(ExportUiState())
    val state: StateFlow<ExportUiState> = _state

    init {
        val isStarted = premiumRepository.getIsStarted()
        _state.value = _state.value.copy(isStarted = isStarted, isAlgorithm = isStarted)
        val isAutoBackup = sessionRepository.getIsAutoBackup()
        _state.value = _state.value.copy(isAutoBackup = isAutoBackup && isStarted)
        if (isStarted) {
            load()
        }
    }

    fun writeDictionaries(sendFile: (File) -> Unit) {
        viewModelScope.launch {
            val file = generateBackupFile(true)
            withContext(Dispatchers.Main) {
                file?.let(sendFile)
            }
        }
    }

    fun changeExported(isExported: Boolean) {
        _state.value = _state.value.copy(isExported = isExported)
    }

    private suspend fun generateBackupFile(fileNameWithDate: Boolean): File? {
        val accountId = sessionRepository.getAccountId() ?: return null
        return withContext(Dispatchers.Default) {
            val list = dictionaryRepository.loadDictionaries(accountId)
            return@withContext exportGenerator.writeDictionaries(
                list,
                state.value.isAlgorithm,
                fileNameWithDate
            )
        }
    }

    fun changeIsAlgorithmChecked(isAlgorithm: Boolean) {
        _state.value = _state.value.copy(isAlgorithm = isAlgorithm)
    }

    private fun load() {
        viewModelScope.launch {
            accountsRepository.getAccount()
                .onEach { _state.value = _state.value.copy(loadingDiskState = it.toState()) }
                .takeWhile { it !is Result.Success }
                .collect()
            _state.value = _state.value.copy(isAuthorization = true)
        }
    }

    fun uploadBackupToGoogleDisk() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = _state.value.copy(isLoadingFile = true)
            val file = generateBackupFile(true)
            file?.let {
                filesRepository.upload(file)
            }
            navigator.toast(R.string.file_has_been_uploaded_successfully)
            _state.value = _state.value.copy(
                isLoadingFile = false,
                isExported = true
            )
        }
    }

    fun signIn() {
        errorHandler.launchIn(viewModelScope) {
            accountsRepository.oauthSignIn()
            sessionRepository.saveIsAutoBackup(true)
            _state.value = _state.value.copy(isAuthorization = true) //TODO наверное не нужно?
        }
    }

    fun changeIsAutoBackUp() {
        val newStateAutoBackup = _state.value.isAutoBackup.not()
        _state.value = _state.value.copy(isAutoBackup = newStateAutoBackup)
        sessionRepository.saveIsAutoBackup(newStateAutoBackup)
    }

    private fun Result<GoogleSignInAccount>.toState(): State {
        return when (this) {
            is Result.Pending -> State.Loading
            is Result.Success -> State.Success
            is Result.Error -> {
                if (exception is AuthException) {
                    State.NotLoggedIn
                } else {
                    State.Error(errorHandler.getErrorMessage(exception))
                }
            }
        }
    }

    sealed class State {
        object Success : State()
        object Loading : State()
        object NotLoggedIn : State()
        class Error(
            val message: String
        ) : State()
    }
}