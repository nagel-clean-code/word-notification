package com.nagel.wordnotification.core.services

import android.os.Bundle
import android.util.Log
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage

class AppHmsMessagingService : HmsMessageService() {
    override fun onNewToken(p0: String?) {
        super.onNewToken(p0)
    }
//TODO https://habr.com/ru/companies/koshelek/articles/522008/ - работа с push и его особенности в hms
    override fun onNewToken(p0: String?, p1: Bundle?) {
        super.onNewToken(p0, p1)
    }

    override fun onMessageReceived(p0: RemoteMessage?) {
        Log.d("onMessageReceived::", p0.toString())
        super.onMessageReceived(p0)
    }
}