package com.nagel.wordnotification.presentation.navigator

import androidx.annotation.StringRes
import java.io.Serializable


interface NavigatorV2 {
    val whenActivityActive: MainActivityActions

    fun launch(screen: BaseScreen)
    fun toast(messageId: Int)
    fun getString(@StringRes messageRes: Int): String
    fun goBack(result: Any? = null)
}

interface BaseScreen : Serializable