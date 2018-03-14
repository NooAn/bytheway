package ru.a1024bits.bytheway.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crash.FirebaseCrash
import com.google.firebase.firestore.QuerySnapshot
import io.reactivex.Single
import ru.a1024bits.bytheway.model.FireBaseNotification
import ru.a1024bits.bytheway.model.Response
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.model.contains
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

class DisplayUsersViewModel @Inject constructor(private var userRepository: UserRepository?) : BaseViewModel(), FilterAndInstallListener {
    var response: MutableLiveData<Response<List<User>>> = MutableLiveData()
    var yearsOldUsers = (0..MAX_AGE).mapTo(ArrayList()) { it.toString() }
    override var filter = Filter()

    companion object {
        const val TAG = "showUserViewModel"
    }

    fun getAllUsers(f: Filter, sortString: String) {
        this.filter = f
        userRepository?.installAllUsers(this, sortString)
    }

    override fun filterAndInstallUsers(sortString: String, vararg snapshots: QuerySnapshot) {
        Single.create<MutableList<User>> { stream ->
            try {
                Log.e("LOG get filter users", Thread.currentThread().name)
                var result: MutableList<User> = ArrayList()
                snapshots.map {
                    for (document in it) {
                        try {
                            val user = document.toObject(User::class.java)
                            if (user.cities.size > 0 && user.id != FirebaseAuth.getInstance().currentUser?.uid) {
                                result.add(user)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            FirebaseCrash.report(e)
                        }
                    }
                }
                // if (sortString.isNotEmpty()) result = filterUsersByString(sortString, result)
                /*todo if filter by string XOR filter? then add: else*/
                filterUsersByFilter(result, filter)
                //  result.sortBy { it.dates[START_DATE] }
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
            loadingStatus.value = true
            disposables.add(it.changeUserProfile(map, id)
                    .timeout(TIMEOUT_SECONDS, timeoutUnit)
                    .retry(2)
                    .doAfterTerminate({ loadingStatus.setValue(false) })
                    .subscribe(
                            { Log.e("LOG", "complete") },
                            { t ->
                                response.value = Response.error(t)
                                Log.e("LOG view model", "send User Data", t)
                            }
                    ))
        }
    }

    fun getUsersWithSimilarTravel(paramSearch: Filter) {
        userRepository?.let {
            loadingStatus.value = true
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

    fun getTextFromDates(date: Long?, months: Array<String>): String {
        val calendarStartDate = Calendar.getInstance()
        calendarStartDate.timeInMillis = date ?: 0L
        return getTextDateDayAndMonth(calendarStartDate, months)
    }

    fun getTextFromDates(startDate: Long?, endDate: Long?, months: Array<String>): String {
        val toWord = " - "

        val calendarStartDate = Calendar.getInstance()
        calendarStartDate.timeInMillis = startDate ?: 0L
        val calendarEndDate = Calendar.getInstance()
        calendarEndDate.timeInMillis = endDate ?: 0L

        if (calendarEndDate.timeInMillis == 0L)
            return getTextDateDayAndMonth(calendarStartDate, months)

        if (calendarStartDate.timeInMillis == 0L)
            return getTextDateDayAndMonth(calendarEndDate, months)

        var yearStart = ""
        var yearEnd = ""
        if (calendarStartDate.get(Calendar.YEAR) != calendarEndDate.get(Calendar.YEAR)) {
            yearStart = calendarStartDate.get(Calendar.YEAR).toString()
            yearEnd = calendarEndDate.get(Calendar.YEAR).toString()
        }
        return getTextDate(calendarStartDate, yearStart, months) + toWord + getTextDate(calendarEndDate, yearEnd, months)
    }

    fun filterUsersByString(primaryQuery: String = "", primaryList: MutableList<User>): MutableList<User> {
        return primaryList.filterTo(ArrayList()) {
            it.contains(primaryQuery)
        }
    }

    private fun getTextDate(calendarStartDate: Calendar, yearStart: String, months: Array<String>): String {
        return StringBuilder("").append(getTextDateDayAndMonth(calendarStartDate, months))
                .append(" ")
                .append(yearStart)
                .toString()
    }

    private fun getTextDateDayAndMonth(calendarStartDate: Calendar, months: Array<String>): String {
        return StringBuilder("").append(calendarStartDate.get(Calendar.DAY_OF_MONTH))
                .append(" ")
                .append(months[calendarStartDate.get(Calendar.MONTH)])
                .toString()
    }

    fun filterUsersByFilter(resultUsers: MutableList<User>, filter: Filter) {
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

    fun sendNotifications(ids: String, notification: FireBaseNotification) {
        userRepository?.let {
            disposables.add(it.sendNotifications(ids, notification)
                    .timeout(TIMEOUT_SECONDS, timeoutUnit)
                    .retry(2)
                    .subscribeOn(getBackgroundScheduler())
                    .subscribe(
                            { Log.e("LOG", "notify complete") },
                            { t ->
                                Log.e("LOG view model", "notify", t)
                            }
                    )
            )
        }

    }
}

interface FilterAndInstallListener {
    var filter: Filter
    fun filterAndInstallUsers(sortString: String, vararg snapshots: QuerySnapshot)
    fun onFailure(e: Throwable)
}