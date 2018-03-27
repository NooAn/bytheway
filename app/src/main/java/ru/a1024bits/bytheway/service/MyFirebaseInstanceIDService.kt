package ru.a1024bits.bytheway.service

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crash.FirebaseCrash
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import ru.a1024bits.bytheway.repository.COLLECTION_USERS
import ru.a1024bits.bytheway.util.Constants

class MyFirebaseInstanceIDService : FirebaseInstanceIdService() {

    private val preferences: SharedPreferences by lazy {
        getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE)
    }

    /**
     * shell am startservice -a com.google.firebase.INSTANCE_ID_EVENT --es "CMD" "RST" -n ru.a1024bits.bytheway/ru.a1024bits.bytheway.service.MyFirebaseInstanceIDService
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onTokenRefresh() {
        // Log.d("log", "Refreshed token: " + FirebaseInstanceId.getInstance().token!!)
        val token = FirebaseInstanceId.getInstance().token
        preferences.edit().putString(Constants.FCM_CMD_UPDATE, token).apply() // for save на всякий случай! Если что у нас уже есть токены и мы можем их достать.
        try {
            FirebaseFirestore.getInstance().runTransaction({
                val currentUid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
                if (currentUid.isNotEmpty() && token != null && token.isNotBlank()) {

                    val docRef = FirebaseFirestore.getInstance().collection(COLLECTION_USERS)
                            .document(currentUid)
                    docRef.update(Constants.FCM_TOKEN, token)
                }
            }).addOnSuccessListener {
                preferences.edit().putBoolean(Constants.FCM_TOKEN, true).apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            FirebaseCrash.report(e)
        }

    }
}
