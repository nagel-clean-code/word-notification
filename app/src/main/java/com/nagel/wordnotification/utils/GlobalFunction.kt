package com.nagel.wordnotification.utils

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
}