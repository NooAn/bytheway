package ru.a1024bits.bytheway.viewmodel

import android.arch.lifecycle.ViewModel
import android.util.Log
import ru.a1024bits.bytheway.MockWebService
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.MockUserRepository

/**
 * Created by andrey.gusenkov on 25/09/2017.
 */
class ShowUsersViewModel : ViewModel() {
    companion object {
        var idCurrentUser = 0
    }
    var userRepo: MockUserRepository? = null
    
    fun init() {
        Log.e("LOG", "init")
        this.userRepo = MockUserRepository(object : MockWebService {
            override fun getChanUsers(fromCount: Long, count: Int): List<User> {
                val result: MutableList<User> = mutableListOf()
                for (e in 1..20) {
                    result.add(User("" + ++idCurrentUser, "" + idCurrentUser, idCurrentUser))
                }
                return result
            }
        })
    }
    
}