package ru.a1024bits.bytheway.service

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import ru.a1024bits.bytheway.util.Constants

class MyFirebaseInstanceIDService : FirebaseInstanceIdService() {
    private val preferences: SharedPreferences by lazy {
        getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE)
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onTokenRefresh() {
       // Log.d("log", "Refreshed token: " + FirebaseInstanceId.getInstance().token!!)
        preferences.edit().putBoolean(Constants.FCM_TOKEN, true).apply()
    }
}
