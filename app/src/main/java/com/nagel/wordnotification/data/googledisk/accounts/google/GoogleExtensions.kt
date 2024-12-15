package com.nagel.wordnotification.data.googledisk.accounts.google

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.WorkerThread
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.api.services.drive.DriveScopes
import com.google.gson.Gson
import com.nagel.wordnotification.Constants.GOOGLE_SIGN_IN_ACCOUNT
import com.nagel.wordnotification.app.App
import com.nagel.wordnotification.domain.googledisk.utils.AuthException
import com.nagel.wordnotification.utils.SharedPrefsUtils
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private const val driveFilesScope = "https://www.googleapis.com/auth/drive.file"
private const val profileScope = "https://www.googleapis.com/auth/userinfo.profile"
private const val emailScope = "https://www.googleapis.com/auth/userinfo.email"

suspend fun <T> Task<T>.suspend() = suspendCoroutine<T> { continuation ->
    addOnSuccessListener {
        continuation.resume(it)
    }
    addOnFailureListener {
        continuation.resumeWithException(it)
    }
}

fun getGoogleSignInClient(context: Context): GoogleSignInClient {
    val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestScopes(Scope(DriveScopes.DRIVE_FILE))
        .build()
    return GoogleSignIn.getClient(context, signInOptions)
}

fun getGoogleLastSignedInAccount(context: Context): GoogleSignInAccount {
    return GoogleSignIn.getLastSignedInAccount(context) ?: run {
        try {
            getGoogleSignInAccount()
        } catch (_: Exception) {
            throw AuthException()
        }
    }
}

fun saveGoogleSignInAccount(googleSignInAccount: GoogleSignInAccount) {
    SharedPrefsUtils.setStringPreference(
        App.get(),
        GOOGLE_SIGN_IN_ACCOUNT,
        Gson().toJson(googleSignInAccount)
    )
}

fun getGoogleSignInAccount(): GoogleSignInAccount {
    val json = SharedPrefsUtils.getStringPreference(App.get(), GOOGLE_SIGN_IN_ACCOUNT)
//    json ?: return null
    return Gson().fromJson(json, GoogleSignInAccount::class.java)
}

@WorkerThread
fun getGoogleAccessToken(context: Context): String? {
    val googleSignInAccount = GoogleSignIn.getLastSignedInAccount(context)
        ?: return null
    val androidAccount = googleSignInAccount.account ?: return null
    val scopesList = listOf("oauth2:", profileScope, driveFilesScope)
    val scopes = scopesList.joinToString(" ")

    val token = GoogleAuthUtil.getToken(
        context,
        androidAccount,
        scopes
    )
    return token
}

class GoogleSignInContract : ActivityResultContract<Unit, Task<GoogleSignInAccount>>() {
    override fun createIntent(context: Context, input: Unit): Intent {
        val client = getGoogleSignInClient(context)
        return client.signInIntent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Task<GoogleSignInAccount> {
        return GoogleSignIn.getSignedInAccountFromIntent(intent)
    }
}