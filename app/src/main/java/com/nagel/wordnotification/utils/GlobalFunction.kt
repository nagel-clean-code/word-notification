package com.nagel.wordnotification.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.nagel.wordnotification.core.analytecs.Analytic
import com.nagel.wordnotification.core.analytecs.ParametersAnalytics
import java.util.UUID

object GlobalFunction {

    fun generateUniqueId(): Int {
        val idOne = UUID.randomUUID()
        var str = "" + idOne
        val uid = str.hashCode()
        val filterStr = "" + uid
        str = filterStr.replace("-".toRegex(), "")
        return str.toInt()
    }

    fun Context.openUrl(link: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("PopupUpdateAppDialog", "Не удалось открыть ссылку", e)
            Analytic.logEvent(
                ParametersAnalytics.EXCEPTION,
                ParametersAnalytics.EXCEPTION_OPEN_LINK, e.toString()
            )
        }
    }
}