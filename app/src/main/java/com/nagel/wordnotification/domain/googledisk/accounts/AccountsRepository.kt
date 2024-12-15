package com.nagel.wordnotification.domain.googledisk.accounts

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.nagel.wordnotification.data.googledisk.accounts.AccountsSource
import com.nagel.wordnotification.domain.googledisk.utils.AuthException
import com.nagel.wordnotification.domain.googledisk.utils.InternalException
import com.nagel.wordnotification.domain.googledisk.utils.Result
import com.nagel.wordnotification.domain.googledisk.utils.ignoreErrors
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onSubscription
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountsRepository @Inject constructor(
    private val accountsSource: AccountsSource
) {

    private val accountsFlow = MutableStateFlow<Result<GoogleSignInAccount>>(Result.Pending())

    /**
     * Get the account data of the current logged-in user.
     *
     * The flow may emit exceptions:
     * - [AuthException] if the user is not logged in
     * - [InternalException]
     *
     * @return infinite flow, always success
     */
    fun getAccount(): Flow<Result<GoogleSignInAccount>> {
        return accountsFlow
            .onSubscription {
                emit(Result.Pending())
                ignoreErrors { reloadAccount(silently = true) }
            }
    }


    /**
     * Try to reload the account data.
     * The flow returned by [getAccount] is automatically updated.
     *
     * @throws InternalException
     */
    suspend fun reloadAccount(silently: Boolean = false) {
        try {
            if (!silently) accountsFlow.value = Result.Pending()
            accountsFlow.value = Result.Success(accountsSource.getAccount())
        } catch (e: Exception) {
            accountsFlow.value = Result.Error(e)
            throw e
        }
    }

    /**
     * Try to sign-in via third-party service.
     *
     * The flow returned by [getAccount] is automatically updated.
     *
     * @throws
     */
    suspend fun oauthSignIn() {
        val account = accountsSource.oauthSignIn()
        accountsFlow.value = Result.Success(account)
    }

    suspend fun signOut() {
        accountsSource.signOut()
        accountsFlow.value = Result.Error(AuthException())
    }

}