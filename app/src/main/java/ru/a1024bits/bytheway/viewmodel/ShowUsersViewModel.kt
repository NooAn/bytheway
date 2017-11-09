package ru.a1024bits.bytheway.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.util.Log
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
        userRepository.getUsers()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result) {
                            userLiveData.setValue(document.toObject(User::class.java))
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