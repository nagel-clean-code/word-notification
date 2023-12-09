package com.nagel.wordnotification.utils.common

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.Html
import com.nagel.wordnotification.R

object NavigationUtils {
    fun openGooglePlayStore(context: Context) {
        val appPackageName = context.packageName
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("$MARKET_APP_URI$appPackageName"))
            )
        } catch (e: ActivityNotFoundException) {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("$MARKET_WEB_URI$appPackageName"))
            )
        }
    }

    fun sendEmail(email: String, subject: String?, body: String?, context: Context) {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(MAIL_URI))
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(body))
        try {
            val title = context.getString(R.string.email_to_support_chooser_title)
            context.startActivity(Intent.createChooser(intent, title))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openLinkInBrowser(url: String?, context: Context?) {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context?.startActivity(browserIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun startActivityCheckInternet(
        intent: Intent?,
        context: Context?,
        onError: (() -> Unit) = { }
    ) {
        if (intent == null) return
        if (SystemUtils.isConnection(context)) {
            context?.startActivity(intent)
        } else {
            onError()
            val errorText = context?.getString(R.string.error_no_internet_connection)
            MessageUtils.showToast(errorText, context)
        }
    }

    fun openDialer(
        phoneNumber: String,
        context: Context
    ) {
        try {
            val intent = Intent(
                Intent.ACTION_DIAL,
                Uri.parse(PHONE_URI + phoneNumber)
            )
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // do nothing
        }
    }

    fun shareReference(context: Context?, reference: String?, title: String?) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, reference)
        context?.startActivity(Intent.createChooser(shareIntent, title))
    }

    private const val MARKET_APP_URI = "market://details?id="
    private const val MARKET_WEB_URI = "https://play.google.com/store/apps/details?id="
    private const val MAIL_URI = "mailto:"
    private const val PHONE_URI = "tel:"
}