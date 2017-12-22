package ru.a1024bits.bytheway.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.os.AsyncTask
import android.util.Log
import com.google.firebase.firestore.QuerySnapshot
import ru.a1024bits.bytheway.algorithm.SearchTravelers
import ru.a1024bits.bytheway.model.Response
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.Filter
import ru.a1024bits.bytheway.repository.UserRepository
import javax.inject.Inject

/**
 * Created by andrey.gusenkov on 25/09/2017.
 */
class DisplayUsersViewModel @Inject constructor(var userRepository: UserRepository) : BaseViewModel() {
    var usersLiveData: MutableLiveData<List<User>> = MutableLiveData<List<User>>()
    val loadingStatus = MutableLiveData<Boolean>()
    var response: MutableLiveData<Response<List<User>>> = MutableLiveData()

    val TAG = "showUserViewModel"

    fun getAllUsers(filter: Filter) {
        userRepository.getAllUsers()
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

    fun getUsersWithSimilarTravel(paramSearch: Filter) {
        loadingStatus.setValue(true)
        disposables.add(userRepository.getReallUsers(paramSearch)
                .subscribeOn(getBackgroundScheduler())
                .observeOn(getMainThreadScheduler())
                .doAfterTerminate({ loadingStatus.setValue(false) })
                .subscribe(
                        { list -> response.setValue(Response.success(list)) },
                        { throwable -> response.setValue(Response.error(throwable)) }
                )
        )
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