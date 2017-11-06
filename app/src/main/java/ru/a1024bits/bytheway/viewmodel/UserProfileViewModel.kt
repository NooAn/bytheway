package ru.a1024bits.bytheway.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.UserRepository


/**
 * Created by andrey.gusenkov on 18/09/2017.
 */
class UserProfileViewModel : ViewModel() {
    private var userId: String? = null
    var user: MutableLiveData<User>? = MutableLiveData()
    private var userRepo: UserRepository? = null


    fun init(userId: String) {
    }

}