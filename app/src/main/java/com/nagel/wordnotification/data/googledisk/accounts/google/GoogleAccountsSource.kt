package com.nagel.wordnotification.data.googledisk.accounts.google

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.nagel.wordnotification.data.googledisk.accounts.AccountsSource
import com.nagel.wordnotification.data.googledisk.accounts.ActivityRequired
import com.nagel.wordnotification.domain.googledisk.utils.AlreadyInProgressException
import com.nagel.wordnotification.domain.googledisk.utils.CalledNotFromUiException
import com.nagel.wordnotification.domain.googledisk.utils.InternalException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAccountsSource @Inject constructor(
    @ApplicationContext val applicationContext: Context
) : AccountsSource, ActivityRequired {

    private var isActivityStarted = true
    private var signInLauncher: ActivityResultLauncher<Unit>? = null
    private var completableDeferred: CompletableDeferred<GoogleSignInAccount>? = null

    override fun onActivityCreated(activity: FragmentActivity) {
        signInLauncher = activity.registerForActivityResult(GoogleSignInContract()) { task ->
            task.addOnSuccessListener { googleAccount: GoogleSignInAccount ->
                saveGoogleSignInAccount(googleAccount)
                completableDeferred?.complete(googleAccount)
            }.addOnFailureListener { exception: Exception? ->
                exception?.let {
                    completableDeferred?.completeExceptionally(it)
                } ?: kotlin.run {
                    completableDeferred = null
                }
            }
        }
    }

    // ----- ActivityRequired impl

    override fun onActivityStarted() {
        isActivityStarted = true
    }

    override fun onActivityStopped() {
        isActivityStarted = false
    }

    override fun onActivityDestroyed() {
        this.signInLauncher = null
    }

    // ----- AccountsSource impl

    override suspend fun oauthSignIn(): GoogleSignInAccount {
        if (!isActivityStarted) throw CalledNotFromUiException()
        val signInLauncher = this.signInLauncher ?: throw CalledNotFromUiException()
        if (completableDeferred != null) throw AlreadyInProgressException()

        signInLauncher.launch(Unit)

        return CompletableDeferred<GoogleSignInAccount>().let {
            completableDeferred = it
            it.await()
        }
    }

    override suspend fun getAccount(): GoogleSignInAccount {
        return getGoogleLastSignedInAccount(applicationContext)
    }

    override suspend fun signOut() {
        try {
            getGoogleSignInClient(applicationContext).signOut().suspend()
        } catch (e: Throwable) {
            throw InternalException(e)
        }
    }

    // --- equals/hash-code for correct working of Activity Result API

    override fun equals(other: Any?): Boolean {
        return other?.javaClass?.name?.equals(javaClass.name) ?: false
    }

    override fun hashCode(): Int {
        return javaClass.name.hashCode()
    }
}