package ru.a1024bits.bytheway.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.UserRepository

/**
 * Created by andrey.gusenkov on 25/09/2017.
 */
class MyProfileViewModel : ViewModel() {
    private var userId: String? = null
    var user: LiveData<User>? = null
    private var userRepo: UserRepository? = null
    
    // @Inject
    fun UserProfileViewModel(userRepository: UserRepository) {
        this.userRepo = userRepository
    }

    override fun onCleared() {
        super.onCleared()
    }
    
    fun init(userId: String) {
        user = userRepo?.getUsers(userID = 1)
    }
    
}