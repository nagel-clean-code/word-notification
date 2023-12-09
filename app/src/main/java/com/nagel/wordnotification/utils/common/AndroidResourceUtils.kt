package com.nagel.wordnotification.utils.common

import android.content.Context
import android.content.res.TypedArray
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat

object AndroidResourceUtils {

    fun getDimension(resId: Int, context: Context?): Float {
        return context?.resources?.getDimension(resId) ?: 0f
    }

    fun getString(id: Int, context: Context?): String {
        return try {
            context?.resources?.getString(id) ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    fun getIntArray(id: Int, context: Context?): TypedArray? {
        return try {
            context?.resources?.obtainTypedArray(id)
        } catch (e: Exception) {
            null
        }
    }

    fun getStringArray(id: Int, context: Context?): Array<String> {
        return try {
            context?.resources?.getStringArray(id)
        } catch (e: Exception) {
            null
        } ?: emptyArray()
    }

    @JvmStatic
    @ColorInt
    fun getColor(context: Context?, id: Int?): Int {
        if (id == null || id == 0 || context == null) return -1
        return ContextCompat.getColor(context, id)
    }
}
