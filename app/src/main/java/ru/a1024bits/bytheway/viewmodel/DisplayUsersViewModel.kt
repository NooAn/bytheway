package ru.a1024bits.bytheway.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.os.AsyncTask
import android.util.Log
import com.google.firebase.firestore.QuerySnapshot
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.Filter
import ru.a1024bits.bytheway.repository.UserRepository
import javax.inject.Inject

/**
 * Created by andrey.gusenkov on 25/09/2017.
 */
class DisplayUsersViewModel @Inject constructor(var userRepository: UserRepository) : ViewModel() {

    var listUser: MutableLiveData<List<User>> = MutableLiveData<List<User>>()
    var usersLiveData: MutableLiveData<List<User>> = MutableLiveData<List<User>>()
    var similarUsersLiveData: MutableLiveData<List<User>> = MutableLiveData<List<User>>()
    val TAG = "showUserViewModel"

    fun getAllUsers(filter: Filter) {
        userRepository.getReallUsers()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        InstallUsers().execute(task.result, filter, usersLiveData)
                    }
                }
    }


    fun sendUserData(map: HashMap<String, Any>, id: String) {
        userRepository.changeUserProfile(map, id)
                .addOnCompleteListener {
                    //fixme
                    Log.e("LOG", "${this::class.java.simpleName}: complete user: complete? ${it.isComplete}; successful? ${it.isSuccessful}")
                }
                .addOnFailureListener {
                    Log.e("LOG", "${this::class.java.simpleName}: fail user")
                    //fixme Здесь обработка лоадера и показь пользователю ошибку загрузки ну не здеь а во вью. пример как эт осделать смотри в вью моделаър
                }
                .addOnSuccessListener {
                    Log.e("LOG", "${this::class.java.simpleName}: ok send user")
                    //fixme
                }
    }

    fun getUsersWithSimilarTravel() {
        userRepository.getReallUsers()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val result: MutableList<User> = ArrayList()
                        for (document in task.result) {
                            try {
                                Log.d(TAG, document.id + " => " + document.data)
                                val user = document.toObject(User::class.java)
                                /*
                                This filter;
                                if user hasn't citi then I don't show him
                                 */
                                if (user.cities.size > 0)
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

    class InstallUsers : AsyncTask<Any, Void, Array<Any>>() {
        override fun doInBackground(vararg dataQuery: Any): Array<Any> {
            Thread.sleep(700)
            val filter = dataQuery[1] as Filter
            val result: MutableList<User> = ArrayList()
            for (document in dataQuery[0] as QuerySnapshot) {
                try {
                    val user = document.toObject(User::class.java)

                    if ((filter.startBudget >= 0) && (filter.endBudget > 0))
                        if (user.budget < filter.startBudget || user.budget > filter.endBudget) continue
                    if ((filter.startDate > 0L) && (filter.endDate > 0L))
                        if ((user.dates["start_date"] as Long) < filter.startDate || (user.dates["end_date"] as Long) > filter.endDate) continue
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
            return arrayOf(result, dataQuery[2])
        }

        override fun onPostExecute(result: Array<Any>) {
            super.onPostExecute(result)
            (result[1] as MutableLiveData<List<User>>).setValue(result[0] as List<User>)
        }
    }
}