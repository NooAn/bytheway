package ru.a1024bits.bytheway.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.google.firebase.firestore.DocumentSnapshot
import ru.a1024bits.bytheway.model.Method
import ru.a1024bits.bytheway.model.SocialNetwork
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.Filter
import ru.a1024bits.bytheway.repository.UserRepository
import javax.inject.Inject

/**
 * Created by andrey.gusenkov on 25/09/2017.
 */
class ShowUsersViewModel @Inject constructor(var userRepository: UserRepository) : ViewModel() {

    var listUser: MutableLiveData<List<User>> = MutableLiveData<List<User>>()
    var usersLiveData: MutableLiveData<List<User>> = MutableLiveData<List<User>>()
    var similarUsersLiveData: MutableLiveData<List<User>> = MutableLiveData<List<User>>()
    val TAG = "showUserViewModel"

    fun getAllUsers(filter: Filter) {
        userRepository.getUsers()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val result: MutableList<User> = ArrayList()
                        for (document in task.result) {
                            try {
                                val user = document.toObject(User::class.java)
                                Log.d(TAG, document.id + " => " + document.data)

                                if ((filter.startBudget >= 0) && (filter.endBudget > 0))
                                    if (user.budget < filter.startBudget || user.budget > filter.endBudget) continue
                                if ((filter.startDate > 0L) && (filter.endDate > 0L))
                                    if (user.data < filter.startDate || user.data > filter.endDate) continue
                                if (user.age < filter.startAge || user.age > filter.endAge) continue
                                if (filter.sex != 0)
                                    if (user.sex != filter.sex) continue
                                if ("" != filter.startCity)
                                    if (!user.cities.contains(filter.startCity)) continue
                                if ("" != filter.endCity)
                                    if (!user.cities.contains(filter.endCity)) continue

                                result.add(user)
                            } catch (e: Exception) {
                            }
                        }
                        usersLiveData.setValue(result)
                    } else {
                        Log.w(TAG, "Error getting documents.", task.exception)
                    }
                }
    }

    fun getUsersWithSimilarTravel(filter: Filter) {
        userRepository.getUsers()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val result: MutableList<User> = ArrayList()
                        for (document in task.result) {
                            try {
                                Log.d(TAG, document.id + " => " + document.data)
                                val user = document.toObject(User::class.java)

                                result.add(user)
                            } catch (e: Exception) {
                            }
                        }
                        similarUsersLiveData.setValue(result)
                    } else {
                        Log.w(TAG, "Error getting documents.", task.exception)
                    }
                }
    }
}