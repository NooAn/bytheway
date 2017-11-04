package ru.a1024bits.bytheway.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import ru.a1024bits.bytheway.MockWebService
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.MockUserRepository
import ru.a1024bits.bytheway.repository.UserRepository
import javax.inject.Inject

/**
 * Created by andrey.gusenkov on 25/09/2017.
 */
class ShowUsersViewModel @Inject constructor(var userRepository: UserRepository) : ViewModel() {
    companion object {
        var idCurrentUser = 0
    }

    var mockRepo: MockUserRepository? = null
    var listUser: LiveData<List<User>>? = null

    fun getAllUsers(): LiveData<List<User>> {
        Log.e("LOG", "init repos1 $userRepository")
        if (listUser == null) {
            listUser = MutableLiveData()
            (listUser as MutableLiveData<List<User>>).setValue(userRepository.getUsers())

        }
        return listUser as MutableLiveData<List<User>>
    }


    fun init() {
        Log.e("LOG", "init")
        this.mockRepo = MockUserRepository(object : MockWebService {
            override fun getChanUsers(fromCount: Long, count: Int): List<User> {
                val result: MutableList<User> = mutableListOf()
                for (e in 1..20) {
                    result.add(User("Василий №" + ++idCurrentUser, "Кропоткин - ", idCurrentUser.toLong()))
                }
                return result
            }
        })
    }


}