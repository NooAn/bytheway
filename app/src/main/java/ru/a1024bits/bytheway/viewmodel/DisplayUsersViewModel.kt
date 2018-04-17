package ru.a1024bits.bytheway.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crash.FirebaseCrash
import com.google.firebase.firestore.QuerySnapshot
import io.reactivex.Single
import ru.a1024bits.bytheway.algorithm.SearchTravelers
import ru.a1024bits.bytheway.model.*
import ru.a1024bits.bytheway.repository.Filter
import ru.a1024bits.bytheway.repository.MAX_AGE
import ru.a1024bits.bytheway.repository.UserRepository
import ru.a1024bits.bytheway.util.Constants.END_DATE
import ru.a1024bits.bytheway.util.Constants.FIRST_INDEX_CITY
import ru.a1024bits.bytheway.util.Constants.LAST_INDEX_CITY
import ru.a1024bits.bytheway.util.Constants.START_DATE
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

/**
 * Created by andrey.gusenkov on 25/09/2017.
 */

class DisplayUsersViewModel @Inject constructor(private var userRepository: UserRepository?) : BaseViewModel(), FilterAndInstallListener {
    val response: MutableLiveData<Response<List<User>>> = MutableLiveData()
    val liveData = MutableLiveData<Int>()
    var yearsOldUsers = (0..MAX_AGE).mapTo(ArrayList()) { it.toString() }
    override var filter = Filter()

    companion object {
        const val TAG = "LOG UserRepository"
        const val MIN_LIMIT = 1
    }

    fun getAllUsers(f: Filter?) {
        try {
            this.filter = f ?: filter
            if (users.size != 0 && filter.isNotDefault()) {
                response.postValue(Response.success(users))
            } else
                userRepository?.installAllUsers(this)
        } catch (e: Exception) {
            e.printStackTrace()
            FirebaseCrash.report(e)
            loadingStatus.postValue(false)
        }
    }

    val TWO_MONTHS_IN_MLSECONDS: Long = 5270400000

    override fun filterAndInstallUsers(vararg snapshots: QuerySnapshot) {
        disposables.add(Single.create<MutableList<User>> { stream ->
            try {
                val result: MutableList<User> = ArrayList()
                snapshots.map {
                    for (document in it) {
                        try {
                            //FIXME надо попытаться вынести это в условие запроса для сервера.
                            if (document.exists()) {
                                val user = document.toObject(User::class.java)
                                if (user.cities.size > 0
                                        && user.id != FirebaseAuth.getInstance().currentUser?.uid) {
                                    result.add(user)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            FirebaseCrash.report(e)
                        }
                    }
                }
                filterUsersByOptions(result, filter)
                stream.onSuccess(result)
            } catch (exp: Exception) {
                stream.onError(exp)
            }
        }
                .subscribeOn(getBackgroundScheduler())
                .timeout(TIMEOUT_SECONDS, timeoutUnit, getBackgroundScheduler())
                .retry(2)
                .doOnSubscribe({ loadingStatus.postValue(true) })
                .doAfterTerminate({ loadingStatus.postValue(false) })
                .subscribe({ resultUsers ->
                    Log.e("LOG", "count users: ${resultUsers.size}")
                    if (users.size == 0)
                        users.addAll(resultUsers)
                    response.postValue(Response.success(resultUsers))
                },
                        { throwable -> response.postValue(Response.error(throwable)) }))
    }

    var users: ArrayList<User> = ArrayList<User>()

    override fun onFailure(e: Throwable) {
        response.postValue(Response.error(e))
    }

    fun sendUserData(map: HashMap<String, Any>, id: String) {
        userRepository?.let {
            loadingStatus.value = true
            disposables.add(it.changeUserProfile(map, id)
                    .timeout(TIMEOUT_SECONDS, timeoutUnit, getBackgroundScheduler())
                    .retry(2)
                    .doAfterTerminate({ loadingStatus.postValue(false) })
                    .subscribe(
                            { },
                            { t ->
                                response.postValue(Response.error(t))
                            }
                    ))
        }
    }


    fun getSearchUsers(paramSearch: Filter) {
        userRepository?.let {
            loadingStatus.value = true
            disposables.add(it.getRealUsers()
                    .subscribeOn(getBackgroundScheduler())
                    .timeout(TIMEOUT_SECONDS, timeoutUnit, getBackgroundScheduler())
                    .retry(2)
                    .cache()
                    .filter { user -> user.cities.size > 0 }
                    .map { user ->
                        // run search algorithm.
                        val search = SearchTravelers(filter = paramSearch, user = user)
                        user.percentsSimilarTravel = search.getEstimation()
                        user
                    }
                    .filter { user ->
                        user.percentsSimilarTravel > MIN_LIMIT && user.id != FirebaseAuth.getInstance().currentUser?.uid
                    }
                    .toSortedList(compareByDescending { l -> l.percentsSimilarTravel }) // перед отправкой сортируем по степени похожести маршрута.
                    .observeOn(getMainThreadScheduler())
                    .doOnSuccess { Log.e("LOG 2", Thread.currentThread().name) }
                    .doAfterTerminate({ loadingStatus.postValue(false) })
                    .subscribe(
                            { list ->
                                response.postValue(Response.success(list))
                            },
                            { throwable -> response.postValue(Response.error(throwable)) }
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

    fun filterUsersByOptions(resultUsers: MutableList<User>, filter: Filter) {
        resultUsers.retainAll {
            checkBudget(filter, it) && checkAge(it, filter) && checkSex(filter, it) && checkFirstCity(filter, it)
                    && checkEndCity(filter, it) && checkMethod(it, filter) && checkStartDate(filter, it) && checkEndDate(filter, it)
        }
    }

    private fun checkEndCity(filter: Filter, it: User) =
            ((filter.endCity.isEmpty()) || (it.cities[LAST_INDEX_CITY]?.contains(filter.endCity, true) == true))

    private fun checkFirstCity(filter: Filter, it: User) =
            ((filter.startCity.isEmpty()) || (it.cities[FIRST_INDEX_CITY]?.contains(filter.startCity, true) == true))

    private fun checkSex(filter: Filter, it: User) =
            ((filter.sex == 0) || (it.sex == filter.sex))

    private fun checkAge(it: User, filter: Filter) =
            ((it.age >= filter.startAge && it.age <= filter.endAge))

    private fun checkBudget(filter: Filter, it: User) =
            (!((filter.startBudget >= 0) && (filter.endBudget > 0)) ||
                    (it.budget >= filter.startBudget && it.budget <= filter.endBudget))

    private fun checkMethod(it: User, filter: Filter): Boolean {
        return !((it.method[Method.BUS.link] == true && filter.method[Method.BUS.link] == true)
                || (it.method[Method.HITCHHIKING.link] == true && filter.method[Method.HITCHHIKING.link] == true)
                || (it.method[Method.CAR.link] == true && filter.method[Method.CAR.link] == true)
                || (it.method[Method.PLANE.link] == true && filter.method[Method.PLANE.link] == true)
                || (it.method[Method.TRAIN.link] == true && filter.method[Method.TRAIN.link] == true))
    }

    private fun checkEndDate(filter: Filter, it: User) =
            (filter.endDate == 0L || (it.dates[END_DATE] != null && it.dates[END_DATE] != 0L &&
                    it.dates[END_DATE] ?: 0 <= filter.endDate))

    private fun checkStartDate(filter: Filter, it: User) =
            (filter.startDate == 0L || (it.dates[START_DATE] != null && it.dates[START_DATE] != 0L &&
                    it.dates[START_DATE] ?: 0L >= filter.startDate))

    fun sendNotifications(ids: String, notification: FireBaseNotification) {
        FirebaseCrash.log("send Notification")
        userRepository?.let {
            disposables.add(it.sendNotifications(ids, notification)
                    .timeout(TIMEOUT_SECONDS, timeoutUnit)
                    .retry(2)
                    .subscribeOn(getBackgroundScheduler())
                    .subscribe(
                            { Log.e("LOG", "notify complete") },
                            { t ->
                                FirebaseCrash.report(t)
                                Log.e("LOG view model", "notify", t)
                            }
                    )
            )
        }

    }
}

private fun Filter.isNotDefault(): Boolean = (this == Filter())

interface FilterAndInstallListener {
    var filter: Filter
    fun filterAndInstallUsers(vararg snapshots: QuerySnapshot)
    fun onFailure(e: Throwable)
}