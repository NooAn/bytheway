package ru.a1024bits.bytheway.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.text.Editable
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.COLLECTION_USERS
import ru.a1024bits.bytheway.repository.UserRepository
import javax.inject.Inject

/**
 * Created by andrey.gusenkov on 25/09/2017.
 */
class MyProfileViewModel @Inject constructor(var userRepository: UserRepository) : ViewModel() {
    val user: MutableLiveData<User> = MutableLiveData<User>()
    val load: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    
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
    
    
    fun ifUserNotExistThenSave(currentUser: FirebaseUser?) {
        val store = FirebaseFirestore.getInstance()
        val docRef = store.collection(COLLECTION_USERS).document(currentUser?.uid.toString());
        docRef.get().addOnCompleteListener(object : OnCompleteListener<DocumentSnapshot> {
            override fun onComplete(task: Task<DocumentSnapshot>) {
                if (task.isSuccessful()) {
                    val document = task.getResult();
                    if (!document.exists()) {
                        // Пользователя нет в системе, добавляем.
                        
                        userRepository.addUser(User().apply {
                            name = currentUser?.displayName.toString()
                            id = currentUser?.uid.toString()
                            email = currentUser?.email.toString()
                            phone = currentUser?.phoneNumber.toString()
                            urlPhoto = currentUser?.photoUrl.toString()
                            currentUser?.getUid()
                        }).addOnCompleteListener {
                            load.value = true
                        }.addOnFailureListener({
                            load.value = false
                        })
                        Log.d("LOG", "No such document and create new doc");
                    } else {
                        // Пользователь уже существует и не нужно тогда добавлять его
                        Log.d("LOG", "DocumentSnapshot data: " + task.getResult().getData());
                        load.value = false
                    }
                } else {
                    load.value = false
                    Log.d("LOG", "get failed with ", task.getException());
                }
            }
        }
        )
        
        currentUser?.getUid()
    }
    
}