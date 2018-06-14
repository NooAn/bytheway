package ru.a1024bits.bytheway.viewmodel

import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.randomString
import ru.a1024bits.bytheway.repository.COLLECTION_USERS
import ru.a1024bits.bytheway.repository.UserRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by Bit on 1/4/2018.
 */
class RegistrationViewModel @Inject constructor(private val userRepository: UserRepository, private val authManager: FirebaseAuth,
                                                private val fireStore: FirebaseFirestore) : BaseViewModel(), LifecycleObserver {
    val load: MutableLiveData<Boolean> = MutableLiveData()
    var currentUser: User = User()


    fun registrationUserWithVk(vkUserId: String): Completable =
            Completable.create { emitter ->
                authManager.createUserWithEmailAndPassword("vk_$vkUserId@forVkEmailsInByTheWay.com", "vk_$vkUserId-password")
                        .addOnSuccessListener { emitter.onComplete() }
                        .addOnFailureListener {
                            if (it is FirebaseAuthUserCollisionException)
                                authManager.signInWithEmailAndPassword("vk_$vkUserId@forVkEmails.com", "vk_$vkUserId-password")
                                        .addOnSuccessListener { emitter.onComplete() }
                                        .addOnFailureListener { emitter.onError(it) }
                            else
                                emitter.onError(it)
                        }
            }
                    .subscribeOn(Schedulers.io())

    fun initUserWithVkSignIn(avatarUrlVk: String, firstNameVk: String, lastNameVk: String) {
        currentUser = User().apply {
            urlPhoto = avatarUrlVk
            name = firstNameVk
            lastName = lastNameVk
            authManager.currentUser?.uid?.let { currentUser.id = it }
        }
    }

    fun initUserWithGoogleOrPhoneSignIn() {
        authManager.currentUser?.let { fireBaseUser ->
            currentUser = User().apply {
                with(fireBaseUser.displayName?.split(" ") ?: emptyList()) {
                    name = getOrElse(0) { "" }
                    lastName = getOrElse(1) { "" }
                }
                id = fireBaseUser.uid
                email = fireBaseUser.email.toString()
                phone = fireBaseUser.phoneNumber ?: "+7"
                urlPhoto = fireBaseUser.photoUrl.toString()
            }
        }
    }

    fun setTimestamp(uid: String) {
        disposables.add(userRepository.sendTime(uid)
                .subscribeOn(getBackgroundScheduler())
                .timeout(10, TimeUnit.SECONDS)
                .retry(2)
                .observeOn(getMainThreadScheduler())
                .subscribe(
                        { Log.e("LOG", "complete") },
                        { throwable -> Log.e("LOG", "timestamp", throwable) }
                ))
    }

    fun ifUserNotExistThenSave() {
        fireStore.collection(COLLECTION_USERS).document(currentUser.id).get()
                .addOnSuccessListener { task ->
                    if (!task.exists()) {
                        // Пользователя нет в системе, добавляем.
                        userRepository.addUser(currentUser)
                                .addOnCompleteListener { Log.d("tag", "1111: ${it.isSuccessful}  : ${it.exception} ") }
                                .addOnSuccessListener { load.value = true }
                                .addOnFailureListener { load.value = false }
                    } else {
                        // Пользователь уже существует и не нужно тогда добавлять его
                        load.value = true
                    }
                }
                .addOnFailureListener { load.value = false }
    }

    fun validatePhoneNumber(phoneNumber: String): Boolean =
            phoneNumber.isNotBlank() && phoneNumber.matches(Regex("^\\+?\\d{10,12}$"))

    fun prepareNumber(number: String): String =
            if (number.contains(Regex("^((380)|(7))")))
                (StringBuilder("+").append(number)).toString() else number
}