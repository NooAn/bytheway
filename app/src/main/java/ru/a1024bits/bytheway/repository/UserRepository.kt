package ru.a1024bits.bytheway.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import ru.a1024bits.bytheway.model.User
import javax.inject.Inject


/**
 * Created by andrey.gusenkov on 19/09/2017.
 */
class UserRepository @Inject constructor(val store: FirebaseFirestore) : BaseUsersRepository {
    var TAG = "LOG UserRepository"

    init {
        Log.e("LOG", "init repos2")
    }

    override fun getSimilarUsersTravels(data: Filter, observer: Observer<List<User>>): List<User> {
        //todo
        return getAllUsers(observer)
    }

    //Rx wrapper
    override fun getAllUsers(observer: Observer<List<User>>): ArrayList<User> {
        val listUser = arrayListOf<User>()
        var user : User
        store.collection("users")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result) {
                            Log.d(TAG, document.id + " => " + document.data)
                            user = User()
                            user.name = document.data.getValue("name") as String
                            user.age = document.data.getValue("age") as Long
                            user.lastName = document.data.getValue("last_name") as String
//                            user.percentsSimilarTravel = document.data.getValue("percentsSimilarTravel") as Int
                            listUser.add(user)
                        }
                        observer.onChanged(listUser)
                    } else {
                        Log.w(TAG, "Error getting documents.", task.exception)
                    }
                }

        return listUser
    }

    fun getUserById(userID: Long): LiveData<User>? {
        return null
    }


}