package ru.a1024bits.bytheway.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.util.Log
import android.util.Log.i
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toObservable
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.Filter
import ru.a1024bits.bytheway.repository.UserRepository
import javax.inject.Inject

/**
 * Created by andrey.gusenkov on 25/09/2017.
 */
class ShowUsersViewModel @Inject constructor(var userRepository: UserRepository) : ViewModel() {

    var listUser: MutableLiveData<List<User>> = MutableLiveData<List<User>>()
    var userLiveData: MutableLiveData<User> = MutableLiveData<User>()
    val TAG = "showUserViewModel"

    fun getAllUsers() {
        val user = User()
        userRepository.getUsers()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result) {
                            Log.d(TAG, document.id + " => " + document.data)
                            user.name = document.data.getValue("name") as String
                            user.age = document.data.getValue("age") as Long
                            try {
                                user.lastName = document.data.getValue("lastName") as String
                            } catch (e: NoSuchElementException) {
                                user.lastName = document.data.getValue("last_name") as String
                                Log.d("tag", e.toString())
                            }
                            userLiveData.setValue(user)
                        }
                    } else {
                        Log.w(TAG, "Error getting documents.", task.exception)
                    }
                }
    }


    fun getSimilarUsersTravels(data: Filter, observer: Observer<List<User>>): LiveData<List<User>> {
        Log.e("LOG", "init repos1 $userRepository")
        userRepository.getSimilarUsersTravels(data, observer)
        return listUser as MutableLiveData<List<User>>
    }


}