package com.nagel.wordnotification.utils.common

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.View
import androidx.core.content.ContextCompat

object StatusBarUtils {
    fun setLightStatusBar(activity: Activity?) {
        if (activity == null) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var flags = activity.window.decorView.systemUiVisibility
            flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            activity.window.decorView.systemUiVisibility = flags
        }
    }

    fun clearLightStatusBar(activity: Activity?) {
        if (activity == null) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var flags = activity.window.decorView.systemUiVisibility
            flags = flags xor View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            activity.window.decorView.systemUiVisibility = flags
        }
    }

    fun changeStatusBarColor(color: Int, changeLightStatusBar: Boolean, activity: Activity?) {
        activity ?: return
        if (changeLightStatusBar) clearLightStatusBar(activity)
        activity.window.statusBarColor = ContextCompat.getColor(activity, color)
    }

    fun getStatusBarHeight(context: Context): Int {
        var result = 0
        val resourceId = 0//context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }
}