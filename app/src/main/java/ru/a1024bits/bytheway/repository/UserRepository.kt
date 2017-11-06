package ru.a1024bits.bytheway.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import ru.a1024bits.bytheway.model.User
import javax.inject.Inject


/**
 * Created by andrey.gusenkov on 19/09/2017.
 */
class UserRepository @Inject constructor(val store: FirebaseFirestore) {
    var TAG = "LOG UserRepository"

    init {
        Log.e("LOG", "init repos2")
    }

    //Rx wrapper
    fun getUsers(): Task<QuerySnapshot> {
        return store.collection("users")
                .get()
    }

    fun getUserById(userID: Long): LiveData<User>? {
        return null
    }


}