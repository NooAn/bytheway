package ru.a1024bits.bytheway

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel



/**
 * Created by andrey.gusenkov on 18/09/2017.
 */
class UserProfileViewModel : ViewModel() {
    private var userId: String? = null
    val user: LiveData<User>? = null
    fun init(userId: String) {
        this.userId = userId
    }
    
}