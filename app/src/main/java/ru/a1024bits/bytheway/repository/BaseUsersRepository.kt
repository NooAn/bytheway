package ru.a1024bits.bytheway.repository

import android.arch.lifecycle.Observer
import ru.a1024bits.bytheway.model.User

interface BaseUsersRepository {
    fun getAllUsers(observer: Observer<List<User>>): ArrayList<User>

    fun getSimilarUsersTravels(data: Filter, observer: Observer<List<User>>): List<User>
}