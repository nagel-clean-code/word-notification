package com.nagel.wordnotification.test

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import com.google.gson.Gson
import com.nagel.wordnotification.Constants
import com.nagel.wordnotification.core.services.AlarmReceiver
import com.nagel.wordnotification.core.services.NotificationDto
import java.util.Date


class Test(private val context: Context) {

    private var counter = 0

    fun launch() {
        val dto = NotificationDto("Тест", "ddjdkj", Date().time, counter++, counter)
        val newIntent = Intent(context, AlarmReceiver::class.java)
        newIntent.putExtra(Constants.TAKE_AWAY, Gson().toJson(dto))
        newIntent.putExtra(Constants.TYPE, Constants.TYPE_QUEST)
        context.sendBroadcast(newIntent)
    }

    fun deleteNotifications() {
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val channelList: List<NotificationChannel> = notificationManager.getNotificationChannels()

        var i = 0
        while (channelList != null && i < channelList.size) {
            notificationManager.deleteNotificationChannel(channelList[i].getId())
            i++
        }
    }


}