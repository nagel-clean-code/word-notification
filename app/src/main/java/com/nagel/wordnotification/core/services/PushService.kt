package com.nagel.wordnotification.core.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.nagel.wordnotification.Constants.NOTIFICATION_CHANNEL_ID
import com.nagel.wordnotification.R
import com.nagel.wordnotification.utils.GlobalFunction


class PushService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title
        val content = message.notification?.body
        val url = message.data[URL]
        newNotification(title, content, url)
        super.onMessageReceived(message)
    }

    private fun newNotification(title: String?, content: String?, url: String? = null) {
        val pendingIntent = if (url != null) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setData(Uri.parse(url))
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            PendingIntent.getActivity(
                applicationContext, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            null
        }
        val notificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nc = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                applicationContext.resources.getString(R.string.app_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(nc)
        }

        val customNotification =
            NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.arrow)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentTitle(title)
                .setContentText(content)

        notificationManager.notify(GlobalFunction.generateUniqueId(), customNotification.build())
    }

    companion object {
        private const val URL = "url"
    }
}