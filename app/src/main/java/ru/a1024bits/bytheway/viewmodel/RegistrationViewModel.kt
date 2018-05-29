package ru.a1024bits.bytheway.viewmodel

import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.COLLECTION_USERS
import ru.a1024bits.bytheway.repository.UserRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by Bit on 1/4/2018.
 */
class RegistrationViewModel @Inject constructor(var userRepository: UserRepository) : BaseViewModel(), LifecycleObserver {
    val load: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    fun setTimestamp(uid: String) {
        disposables.add(userRepository.sendTime(uid)
                .subscribeOn(getBackgroundScheduler())
                .timeout(10, TimeUnit.SECONDS)
                .retry(2)
                .observeOn(getMainThreadScheduler())
                .subscribe(
                        { Log.e("LOG", "complete") },
                        { throwable -> Crashlytics.logException(throwable) }
                ))
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
                            if (list?.getOrNull(1) != null) lastName = list[1]
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
                    } else {
                        // Пользователь уже существует и не нужно тогда добавлять его
                        load.value = true
                    }
                } else {
                    load.value = false
                }
            }
        })
    }
}