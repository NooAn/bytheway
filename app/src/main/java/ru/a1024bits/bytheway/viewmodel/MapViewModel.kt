package ru.a1024bits.bytheway.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.UserRepository
import javax.inject.Inject

/**
 * Created by tikhon.osipov on 26.11.17
 */
class MapViewModel @Inject constructor(var userRepository: UserRepository) : ViewModel() {
    val load: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    val error: MutableLiveData<Int> = MutableLiveData<Int>()

}