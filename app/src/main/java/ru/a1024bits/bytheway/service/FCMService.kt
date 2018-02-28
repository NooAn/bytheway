package ru.a1024bits.bytheway.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.support.v4.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

import android.graphics.BitmapFactory
import android.support.v4.content.LocalBroadcastManager
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.ui.activity.MenuActivity
import ru.a1024bits.bytheway.util.Constants
import ru.a1024bits.bytheway.util.Constants.NOTIFICATION_CMD
import ru.a1024bits.bytheway.util.Constants.NOTIFICATION_TITLE
import ru.a1024bits.bytheway.util.Constants.NOTIFICATION_VALUE
import android.app.NotificationChannel
import android.os.Build


class FCMService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {

        var notificationTitle: String? = null
        var notificationBody: String? = null
        var dataValue: String? = null
        var dataCmd: String? = null

        val data = remoteMessage?.getData()

        if (data != null) {
            dataCmd = data[NOTIFICATION_CMD]
            dataValue = data[NOTIFICATION_VALUE]
            if (data.containsKey(NOTIFICATION_TITLE)) notificationTitle = data[NOTIFICATION_TITLE]
        }

        if (remoteMessage?.notification != null) {
            notificationTitle = remoteMessage.notification?.title
            notificationBody = remoteMessage.notification?.body
        }

        if (dataCmd?.startsWith("fcm_") == true) {
            val intent = Intent(Constants.FCM_SRV)
            intent.putExtra(NOTIFICATION_CMD, dataCmd)
            intent.putExtra(NOTIFICATION_VALUE, dataValue)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        } else {
            val intent = Intent(this, MenuActivity::class.java)
            intent.putExtra(NOTIFICATION_CMD, dataCmd)
            intent.putExtra(NOTIFICATION_VALUE, dataValue)
            createNotification(notificationTitle, notificationBody, intent)
        }
    }

    private fun createNotification(notificationTitle: String?, notificationBody: String?, intent: Intent) {

        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT)
        val channelId = getString(R.string.app_name)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.icon_logo))
                .setSmallIcon(R.drawable.icon_logo_small)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                    "bytheway",
                    NotificationManager.IMPORTANCE_DEFAULT)

            notificationManager.createNotificationChannel(channel)

        }
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }
}