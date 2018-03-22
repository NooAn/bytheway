package ru.a1024bits.bytheway.service

import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.util.Log
import ru.a1024bits.bytheway.ui.activity.MenuActivity
import ru.a1024bits.bytheway.util.Constants


/**
 * Created by Andrei_Gusenkov on 3/22/2018.
 */
class ReferListener : BroadcastReceiver() {
    override fun onReceive(context: Context, refferer: Intent) {
        val rawReferrerString = refferer.getStringExtra("referrer")
        if (rawReferrerString != null) {
            Log.i("LOGGER", "Received the following intent $rawReferrerString")
            val intent = Intent(context, MenuActivity::class.java)
            intent.putExtra(Constants.NOTIFICATION_CMD, Constants.GOOGLE_PLAY_CMD_SHOW_USER)
            intent.putExtra(Constants.NOTIFICATION_VALUE, rawReferrerString)
            context.startActivity(intent)
        }
    }
}