package com.nagel.wordnotification.data.googledisk.accounts

import com.google.android.gms.auth.api.signin.GoogleSignInAccount

interface AccountsSource {

    /**
     * Try to sign-in.
     * @throws CalledNotFromUiException
     * @throws LoginFailedException
     * @throws InternalException
     */
    suspend fun oauthSignIn(): GoogleSignInAccount

    /**
     * Get the current signed-in account.
     * @throws AuthException if the user is not logged-in
     * @throws InternalException
     */
    suspend fun getAccount(): GoogleSignInAccount

    /**
     * Sign-out from the app.
     * @throws InternalError
     */
    suspend fun signOut()

}