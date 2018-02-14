package ru.a1024bits.bytheway.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import ru.a1024bits.bytheway.repository.COLLECTION_USERS


class Utils {

    companion object {
        fun updateFcmToken() {
            val token = FirebaseInstanceId.getInstance().token
            if (token != null && token.isNotEmpty()) {
                val currentUid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
                if (currentUid.isNotEmpty()) {
                    val docRef = FirebaseFirestore.getInstance().collection(COLLECTION_USERS)
                            .document(currentUid)
                    docRef.update(Constants.FCM_TOKEN, token)
                }
            }
        }
    }
}