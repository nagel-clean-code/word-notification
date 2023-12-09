package com.nagel.wordnotification.utils.common

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.nagel.wordnotification.R
import java.util.Locale
import java.util.Objects

class SystemUtils(
    val context: Context
) {

    @SuppressLint("MissingPermission")
    fun isConnected(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = Objects.requireNonNull(cm).activeNetworkInfo
        return networkInfo != null && networkInfo.isConnectedOrConnecting
    }

    companion object {
        private const val ID_KEY = "android_id"
        private const val URI_GSF_CONTENT_PROVIDER = "content://com.google.android.gsf.gservices"
        private const val PLAY_MARKET_PACKAGE = "com.android.vending"
        private const val RADIX = 16

        fun getAppVersion(context: Context): String? =
            getPackageInfo(context, context.packageName)?.versionName

        fun getDeviceId(context: Context?): String? =
            getGsfId(context) ?: getAndroidId(context)

        val deviceName: String
            get() {
                val manufacturer = Build.MANUFACTURER
                val model = Build.MODEL
                return if (model.startsWith(manufacturer)) {
                    model
                } else {
                    "$manufacturer $model".uppercase(Locale.getDefault())
                }
            }

        @SuppressLint("MissingPermission")
        fun isConnection(context: Context?): Boolean {
            if (context == null) {
                return false
            }
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = Objects.requireNonNull(cm).activeNetworkInfo
            return networkInfo != null && networkInfo.isConnectedOrConnecting
        }

        @SuppressLint("MissingPermission")
        fun isConnection(activity: AppCompatActivity): Boolean {
            val cm =
                activity.baseContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = cm.activeNetworkInfo
            val isConnection = networkInfo != null && networkInfo.isConnectedOrConnecting
            if (!isConnection) {
                MessageUtils.showTopMessageRed(R.string.main_msg_NoNetworkConnection, activity)
            }
            return isConnection
        }

        fun getCountryName(countryCode: String?): String {
            return Locale(Locale.getDefault().displayLanguage, countryCode.orEmpty()).displayCountry
        }

        fun isGooglePlayAvailable(context: Context): Boolean =
            isGooglePlayServicesAvailable(context) && getPackageInfo(
                context = context,
                packageName = PLAY_MARKET_PACKAGE
            ) != null

        fun isGooglePlayServicesAvailable(context: Context): Boolean {
            val googleApiAvailability = GoogleApiAvailability.getInstance()
            val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
            return resultCode == ConnectionResult.SUCCESS
        }

        private fun getPackageInfo(context: Context, packageName: String): PackageInfo? =
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.packageManager.getPackageInfo(
                        packageName,
                        PackageManager.PackageInfoFlags.of(0)
                    )
                } else {
                    context.packageManager.getPackageInfo(
                        packageName,
                        0
                    )
                }
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }

        private fun getGsfId(context: Context?): String? =
            ExceptionUtils.tryOrNull {
                var result: String? = null
                val params = arrayOf(ID_KEY)
                val uri = Uri.parse(URI_GSF_CONTENT_PROVIDER)
                val cursor = context?.contentResolver
                    ?.query(uri, null, null, params, null)
                cursor.use {
                    if (cursor != null && cursor.moveToFirst() && cursor.columnCount > 1) {
                        result = cursor.getString(1)
                    }
                }
                result
            }

        @SuppressLint("HardwareIds")
        private fun getAndroidId(context: Context?): String? =
            ExceptionUtils.tryOrNull {
                Settings.Secure.getString(
                    context?.contentResolver,
                    Settings.Secure.ANDROID_ID
                ).toBigInteger(RADIX).toString()
            }

    }
}