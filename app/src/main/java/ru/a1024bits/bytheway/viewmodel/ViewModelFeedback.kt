package ru.a1024bits.bytheway.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.UserRepository
import javax.inject.Inject

/**
 * Created by x220 on 28.11.2017.
 */
class ViewModelFeedback @Inject constructor(var userRepository: UserRepository): ViewModel() {
    fun sendFeedback(toString: String) {

    }

}