package ru.a1024bits.bytheway.viewmodel

import android.arch.lifecycle.ViewModel
import android.util.Log
import ru.a1024bits.bytheway.MockWebService
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.MockUserRepository
import java.util.*

/**
 * Created by andrey.gusenkov on 25/09/2017.
 */
class ShowUsersViewModel() : ViewModel() {
    var userRepo: MockUserRepository? = null
    
    fun init() {
        Log.e("LOG", "init");
        this.userRepo = MockUserRepository(object : MockWebService {
            val r = Random(2000000000L)
            override fun getChanUsers(fromCount: Long, count: Int): List<User> {
                val result: MutableList<User> = mutableListOf()
                for (e in 1..20) {
                    result.add(User("" + r.nextLong(), "" + r.nextLong(), r.nextInt()))
                }
                return result
            }
        })
    }
    
}