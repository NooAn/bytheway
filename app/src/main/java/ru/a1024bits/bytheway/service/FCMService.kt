package ru.a1024bits.bytheway.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.support.v4.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

import android.app.ActivityManager
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.ui.activity.MenuActivity
import ru.a1024bits.bytheway.util.Constants

class FCMService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {

        var notificationTitle: String? = null
        var notificationBody: String? = null
        var dataValue: String? = null
        var dataCmd: String? = null
        var notify: String? = null

        val notificationEnabled = true //todo
        val inAppNotificationEnabled = true //todo
        val data = remoteMessage?.getData()

        if (data != null) {
            dataCmd = data["cmd"]
            dataValue = data["value"]
            notify = data["notify"]
            notificationTitle = data["title"]
        }

        if (remoteMessage?.notification != null) {
            notificationTitle = remoteMessage.notification?.title
            notificationBody = remoteMessage.notification?.body
        }

        if (notificationEnabled) {
            var foregroundMessage = applicationInForeground() && notify.isNullOrEmpty()
            if (dataCmd?.startsWith("fcm_") == true) {
                foregroundMessage = true
            }
            if (foregroundMessage) {
                if (inAppNotificationEnabled) {
                    val intent = Intent(Constants.FCM_SRV)
                    intent.putExtra("cmd", dataCmd)
                    intent.putExtra("value", dataValue)
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                }
            } else {
                val intent = Intent(this, MenuActivity::class.java)
                intent.putExtra("cmd", dataCmd)
                intent.putExtra("value", dataValue)
                sendNotification(notificationTitle, notificationBody, intent)
            }
        }
    }

    private fun sendNotification(notificationTitle: String?, notificationBody: String?, intent: Intent) {

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT)

        val channelId = getString(R.string.app_name)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_car_grey)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }

    private fun applicationInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val services = activityManager.runningAppProcesses
        var isActivityFound = false

        if (services[0].processName.equals(packageName, ignoreCase = true)) {
            isActivityFound = true
        }

        return isActivityFound
    }
}