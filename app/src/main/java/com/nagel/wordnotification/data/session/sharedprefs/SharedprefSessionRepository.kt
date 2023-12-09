package com.nagel.wordnotification.data.session.sharedprefs

import android.content.Context
import com.google.gson.Gson
import com.nagel.wordnotification.data.accounts.entities.Account
import com.nagel.wordnotification.data.session.SessionRepository
import com.nagel.wordnotification.data.session.entities.SessionDataEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SharedprefSessionRepository @Inject constructor(
    @ApplicationContext val context: Context
) : SessionRepository {

    private var sessionEntityBuf: SessionDataEntity? = null
    private val sharedPreferences =
        context.getSharedPreferences(SHARED_PREFS_SESSION, Context.MODE_PRIVATE)

    private var currentCashSessionDataEntity: SessionDataEntity? = null

    init {
        CoroutineScope(Dispatchers.IO).launch {
            val currentTime = Date().time
            sessionEntityBuf = getSession() ?: SessionDataEntity(dateAppInstallation = currentTime)
            sessionEntityBuf!!.apply {
                dateAppInstallation ?: run {
                    dateAppInstallation = currentTime
                    ratedApp = false
                    stepRatedApp = 0
                    saveSession(this@apply)
                }
            }
        }
    }

    override suspend fun saveAccount(account: Account) {
        sessionEntityBuf!!.account = account
        saveSession(sessionEntityBuf!!)
    }


    override suspend fun saveSession(data: SessionDataEntity) {
        val json = Gson().toJson(data)
        sharedPreferences.edit().putString(SESSiON_STATE, json).apply()
        currentCashSessionDataEntity = data
    }

    override fun getSession(): SessionDataEntity? {
        val json = sharedPreferences.getString(SESSiON_STATE, "")
        currentCashSessionDataEntity = if (json?.isBlank() == true) {
            null
        } else {
            Gson().fromJson(json, SessionDataEntity::class.java)
        }
        return currentCashSessionDataEntity
    }

    companion object {
        private const val SESSiON_STATE = "SESSiON_STATE"
        private const val SHARED_PREFS_SESSION = "SHARED_PREFS_SESSION"
    }
}