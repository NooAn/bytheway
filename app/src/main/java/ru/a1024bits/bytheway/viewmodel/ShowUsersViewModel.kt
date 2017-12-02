package ru.a1024bits.bytheway.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
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
//                        Log.d("tag", "filter: " + "filter.endAge: " + filter.endAge + " filter.startAge: " + filter.startAge + " filter.startDate: " + filter.startDate + " filter.endDate: " + filter.endDate + " filter.startBudget: " + filter.startBudget + " filter.endBudget: " + filter.endBudget + " filter.sex: " + filter.sex)
                        Log.d("tag", "filter: " + filter)
                        val result: MutableList<User> = ArrayList()
                        for (document in task.result) {
                            try {
                                val user = User()
                                Log.d(TAG, document.id + " => " + document.data)
                                initializeUserFromDocument(user, document)

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
                                val user = User()
                                Log.d(TAG, document.id + " => " + document.data)
                                initializeUserFromDocument(user, document)

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

    private fun initializeUserFromDocument(user: User, document: DocumentSnapshot) {
        user.lastName = document.data.getValue("lastName") as String
        user.email = document.data.getValue("email") as String
        user.name = document.data.getValue("name") as String
        if (document.data.containsKey("age"))
            user.age = document.data.getValue("age") as Long
        if (document.data.containsKey("budget"))
            user.budget = document.data.getValue("budget") as Long
        if (document.data.containsKey("method"))
            user.method = document.data.getValue("method") as ArrayList<Method>
        if (document.data.containsKey("socialNetwork"))
            user.socialNetwork = document.data.getValue("socialNetwork") as HashMap<String, String>
        if (document.data.containsKey("dates"))
            user.dates = document.data.getValue("dates") as ArrayList<Long>
        if (document.data.containsKey("route"))
            user.route = document.data.getValue("route") as String
        if (document.data.containsKey("cities"))
            user.cities = document.data.getValue("cities") as ArrayList<String>
        if (document.data.containsKey("urlPhoto"))
            user.urlPhoto = document.data.getValue("urlPhoto") as String
        if (document.data.containsKey("phone"))
            user.phone = document.data.getValue("phone") as String
        if (document.data.containsKey("sex"))
            user.sex = (document.data.getValue("sex") as Long).toInt()
        if (document.data.containsKey("budget"))
            user.budget = document.data.getValue("budget") as Long
        if (document.data.containsKey("id"))
            user.id = document.data.getValue("id") as String
        if (document.data.containsKey("data"))
            user.data = document.data.getValue("data") as Long
        if (document.data.containsKey("city"))
            user.city = document.data.getValue("city") as String
    }

}