package com.nagel.wordnotification.data.googledisk.accounts

import androidx.fragment.app.FragmentActivity

interface ActivityRequired {

    fun onActivityCreated(activity: FragmentActivity)

    fun onActivityStarted()

    fun onActivityStopped()

    fun onActivityDestroyed()

}