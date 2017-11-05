package ru.a1024bits.bytheway.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.util.Log
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.UserRepository
import javax.inject.Inject

/**
 * Created by andrey.gusenkov on 25/09/2017.
 */
class ShowUsersViewModel @Inject constructor(var userRepository: UserRepository) : ViewModel() {
    var listUser: LiveData<List<User>>? = null

    fun getAllUsers(observer: Observer<List<User>>): LiveData<List<User>> {
        Log.e("LOG", "init repos1 $userRepository")
        if (listUser == null) {
            listUser = MutableLiveData()
            (listUser as MutableLiveData<List<User>>).value = userRepository.getUsers(observer)
        }
        return listUser as MutableLiveData<List<User>>
    }
}