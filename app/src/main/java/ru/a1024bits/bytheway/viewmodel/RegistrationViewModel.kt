package ru.a1024bits.bytheway.viewmodel

import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.COLLECTION_USERS
import ru.a1024bits.bytheway.repository.UserRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by Bit on 1/4/2018.
 */
class RegistrationViewModel @Inject constructor(var userRepository: UserRepository) : ViewModel(), LifecycleObserver {
    val load: MutableLiveData<Boolean> = MutableLiveData<Boolean>()

    fun setTimestamp(uid: String) {
        userRepository.sendTime(uid)
                .subscribeOn(Schedulers.io())
                .timeout(10, TimeUnit.SECONDS)
                .retry(2)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }

    fun ifUserNotExistThenSave(currentUser: FirebaseUser?) {
        val docRef = FirebaseFirestore.getInstance().collection(COLLECTION_USERS).document(currentUser?.uid.toString());
        docRef.get().addOnCompleteListener(object : OnCompleteListener<DocumentSnapshot> {
            override fun onComplete(task: Task<DocumentSnapshot>) {
                if (task.isSuccessful()) {
                    val document = task.getResult()
                    if (!document.exists()) {
                        // Пользователя нет в системе, добавляем.
                        userRepository.addUser(User().apply {
                            val list = currentUser?.displayName?.split(" ")
                            name = list?.get(0).orEmpty()
                            lastName = list?.get(1).orEmpty()
                            id = currentUser?.uid.orEmpty()
                            email = currentUser?.email.toString()
                            phone = currentUser?.phoneNumber ?: "+7"
                            urlPhoto = currentUser?.photoUrl.toString()
                            currentUser?.getUid()
                        }).addOnCompleteListener {
                            load.value = true
                        }.addOnFailureListener {
                                    load.value = false
                                }
                        Log.d("LOG", "No such document and create new doc");
                    } else {
                        // Пользователь уже существует и не нужно тогда добавлять его
                        Log.d("LOG", "DocumentSnapshot data: " + task.getResult().getData());
                        load.value = true
                    }
                } else {
                    load.value = false
                    Log.d("LOG", "get failed with ", task.getException());
                }
            }
        })
    }
}