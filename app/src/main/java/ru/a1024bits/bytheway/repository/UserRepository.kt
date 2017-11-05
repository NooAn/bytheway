package ru.a1024bits.bytheway.repository

import android.arch.lifecycle.LiveData
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
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
    fun getUsers(): ArrayList<User> {
        val user = User()
        val listUser = arrayListOf<User>()
        store.collection("users")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result) {
                            Log.d(TAG, document.id + " => " + document.data)
                            user.name = document.data.getValue("name") as String
                            user.age = document.data.getValue("age") as Long
                            user.lastName = document.data.getValue("last_name") as String
                            listUser.add(user)
                        }
                    } else {
                        Log.w(TAG, "Error getting documents.", task.exception)
                    }
                }

        return listUser;
    }

    fun getUserById(userID: Long): User {
        return User("Andrey");
    }


}