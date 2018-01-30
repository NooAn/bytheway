package ru.a1024bits.bytheway.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.QuerySnapshot
import io.reactivex.Single
import ru.a1024bits.bytheway.App
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.model.Response
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.Filter
import ru.a1024bits.bytheway.repository.MAX_AGE
import ru.a1024bits.bytheway.repository.UserRepository
import ru.a1024bits.bytheway.util.Constants.END_DATE
import ru.a1024bits.bytheway.util.Constants.FIRST_INDEX_CITY
import ru.a1024bits.bytheway.util.Constants.LAST_INDEX_CITY
import ru.a1024bits.bytheway.util.Constants.START_DATE
import java.util.*
import javax.inject.Inject

/**
 * Created by andrey.gusenkov on 25/09/2017.
 */

class DisplayUsersViewModel @Inject constructor(var userRepository: UserRepository?) : BaseViewModel(), FilterAndInstallListener {
    var response: MutableLiveData<Response<List<User>>> = MutableLiveData()
    var yearsOldUsers = (0..MAX_AGE).mapTo(ArrayList<String>()) { it.toString() }
    var filter = Filter()

    companion object {
        const val TAG = "showUserViewModel"
    }

    fun getAllUsers(f: Filter) {
        this.filter = f
        userRepository?.installAllUsers(this)
    }

    override fun filterAndInstallUsers(snapshot: QuerySnapshot) {
        Single.create<MutableList<User>> { stream ->
            try {
                Log.e("LOG get filter users", Thread.currentThread().name)
                val result: MutableList<User> = ArrayList()
                for (document in snapshot) {
                    try {
                        val user = document.toObject(User::class.java)
                        if (user.cities.size > 0 && user.id != FirebaseAuth.getInstance().currentUser?.uid) {
                            result.add(user)
                        }
                    } catch (e: Exception) {
                    }
                }
                filterUsersByFilter(result, filter)
                stream.onSuccess(result)
            } catch (exp: Exception) {
                stream.onError(exp) // for fix bugs FirebaseFirestoreException: DEADLINE_EXCEEDED
            }
        }
                .subscribeOn(getBackgroundScheduler())
                .timeout(TIMEOUT_SECONDS, timeoutUnit)
                .retry(2)
                .doOnSubscribe({ loadingStatus.postValue(true) })
                .doAfterTerminate({ loadingStatus.postValue(false) })
                .subscribe({ resultUsers ->
                    Log.e("LOG subscribe", Thread.currentThread().name)
                    response.postValue(Response.success(resultUsers))
                },
                        { throwable -> response.postValue(Response.error(throwable)) })
    }

    override fun onFailure(e: Throwable) {
        response.postValue(Response.error(e))
    }

    fun sendUserData(map: HashMap<String, Any>, id: String) {
        userRepository?.let {
            loadingStatus.setValue(true)
            disposables.add(it.changeUserProfile(map, id)
                    .timeout(TIMEOUT_SECONDS, timeoutUnit)
                    .retry(2)
                    .doAfterTerminate({ loadingStatus.setValue(false) })
                    .subscribe(
                            { Log.e("LOG", "complete") },
                            { t ->
                                response.setValue(Response.error(t))
                                Log.e("LOG view model", "send User Data", t)
                            }
                    ))
        }
    }

    fun getUsersWithSimilarTravel(paramSearch: Filter) {
        userRepository?.let {
            loadingStatus.setValue(true)
            disposables.add(it.getReallUsers(paramSearch)
                    .timeout(TIMEOUT_SECONDS, timeoutUnit)
                    .retry(2)
                    .subscribeOn(getBackgroundScheduler())
                    .observeOn(getMainThreadScheduler())
                    .doAfterTerminate({ loadingStatus.setValue(false) })
                    .subscribe(
                            { list -> response.setValue(Response.success(list)) },
                            { throwable -> response.setValue(Response.error(throwable)) }
                    )
            )
        }
    }

    fun getTextFromDates(date: Long?, context: Context): String {
        val calendarStartDate = Calendar.getInstance()
        calendarStartDate.timeInMillis = date ?: 0L
        return getTextDateDayAndMonth(calendarStartDate, context)
    }

    fun getTextFromDates(startDate: Long?, endDate: Long?, context: Context): String {
        val toWord = " - "

        val calendarStartDate = Calendar.getInstance()
        calendarStartDate.timeInMillis = startDate ?: 0L
        val calendarEndDate = Calendar.getInstance()
        calendarEndDate.timeInMillis = endDate ?: 0L

        if (calendarEndDate.timeInMillis == 0L)
            return getTextDateDayAndMonth(calendarStartDate, context)

        if (calendarStartDate.timeInMillis == 0L)
            return getTextDateDayAndMonth(calendarEndDate, context)

        var yearStart = ""
        var yearEnd = ""
        if (calendarStartDate.get(Calendar.YEAR) != calendarEndDate.get(Calendar.YEAR)) {
            yearStart = calendarStartDate.get(Calendar.YEAR).toString()
            yearEnd = calendarEndDate.get(Calendar.YEAR).toString()
        }
        return getTextDate(calendarStartDate, yearStart, context) + toWord + getTextDate(calendarEndDate, yearEnd, context)
    }

    fun filterUsersByString( primaryQuery: String = "", primaryList: MutableList<User>): MutableList<User> {
        val queryLowerCase: String = primaryQuery.toLowerCase()
        return primaryList.filterTo(ArrayList()) {
            it.cities.filterValues { it1 -> it1.toLowerCase().contains(queryLowerCase) }.isNotEmpty() ||
                    it.name.toLowerCase().contains(queryLowerCase) || it.email.toLowerCase().contains(queryLowerCase) ||
                    it.age.toString().contains(queryLowerCase) || it.budget.toString().contains(queryLowerCase) ||
                    it.lastName.toLowerCase().contains(queryLowerCase) || it.phone.contains(primaryQuery) ||
                    it.route.contains(queryLowerCase) || it.addInformation.toLowerCase().contains(primaryQuery)
        }
    }

    private fun getTextDate(calendarStartDate: Calendar, yearStart: String, context: Context): String {
        return StringBuilder("").append(getTextDateDayAndMonth(calendarStartDate, context))
                .append(" ")
                .append(yearStart)
                .toString()
    }

    private fun getTextDateDayAndMonth(calendarStartDate: Calendar, context: Context): String {
        return StringBuilder("").append(calendarStartDate.get(Calendar.DAY_OF_MONTH))
                .append(" ")
                .append(context.resources.getStringArray(R.array.months_array)[calendarStartDate.get(Calendar.MONTH)])
//                .append(month)
                .toString()
    }

    private fun filterUsersByFilter(resultUsers: MutableList<User>, filter: Filter) {
        Log.e("LOG filter", Thread.currentThread().name)
        resultUsers.retainAll {
            var found = (!((filter.startBudget >= 0) && (filter.endBudget > 0)) ||
                    (it.budget >= filter.startBudget && it.budget <= filter.endBudget)) &&
                    ((it.age >= filter.startAge && it.age <= filter.endAge)) &&
                    ((filter.sex == 0) || (it.sex == filter.sex)) &&
                    ((filter.startCity.isEmpty()) ||
                            (it.cities[FIRST_INDEX_CITY]?.contains(filter.startCity, true) == true)) &&
                    ((filter.endCity.isEmpty()) ||
                            (it.cities[LAST_INDEX_CITY]?.contains(filter.endCity, true) == true))
            if (found && filter.startDate > 0L) {
                found = (it.dates[START_DATE] != null && it.dates[START_DATE] != 0L &&
                        it.dates[START_DATE] ?: 0 >= filter.startDate)
            }
            if (found && filter.endDate > 0L) {
                found = (it.dates[END_DATE] != null && it.dates[END_DATE] != 0L &&
                        it.dates[END_DATE] ?: 0 <= filter.endDate)
            }
            found
        }
    }
}

interface FilterAndInstallListener {
    fun filterAndInstallUsers(snapshot: QuerySnapshot)
    fun onFailure(e: Throwable)
}