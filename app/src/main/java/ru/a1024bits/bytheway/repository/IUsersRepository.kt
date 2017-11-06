package ru.a1024bits.bytheway.repository

import android.arch.lifecycle.Observer
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import ru.a1024bits.bytheway.model.User

interface IUsersRepository {
    fun getUsers(): Task<QuerySnapshot>
    fun getSimilarUsersTravels(data: Filter, observer: Observer<List<User>>): List<User>
    fun getUserById(userID: Long): Task<DocumentSnapshot>
    fun changeUserProfile(map: HashMap<String, Any>, id: String): Task<Void>
    fun addUser(user: User): Task<Void>
}