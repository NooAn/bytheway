package ru.a1024bits.bytheway.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.text.Editable
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.UserRepository
import javax.inject.Inject

/**
 * Created by andrey.gusenkov on 25/09/2017.
 */
class MyProfileViewModel @Inject constructor(var userRepository: UserRepository) : ViewModel() {
    private var userId: String? = null
    val user: MutableLiveData<User> = MutableLiveData<User>()

    fun load(userId: Long) {
        Log.e("LOG", "start load user: $userId")
        userRepository.getUserById(userId)
                .addOnFailureListener {
                    Log.e("LOG", "error ${it.message}")
                }
                .addOnSuccessListener { document ->
                    val profile = User()
                    profile.name = document.data.getValue("name") as String
                    profile.age = document.data.getValue("age") as Long
                    profile.lastName = document.data.getValue("last_name") as String
                    user.setValue(profile)
                }
        Log.e("LOG", "end load user: $userId")
    }

    fun saveLinks(textLinks: Editable) {
        Log.e("LOG", textLinks.toString())
    }

}