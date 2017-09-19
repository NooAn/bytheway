package ru.a1024bits.bytheway.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.UserRepository
import javax.inject.Inject


/**
 * Created by andrey.gusenkov on 18/09/2017.
 */
class UserProfileViewModel : ViewModel() {
    private var userId: String? = null
    var user: LiveData<User>? = null
    private var userRepo: UserRepository? = null
    
    @Inject
    fun UserProfileViewModel(userRepository: UserRepository) {
        this.userRepo = userRepository
    }
    
    fun init(userId: String) {
        if (user != null) {
            return
        }
        user = userRepo!!.getUsers(userID = 1)
    }
    
}