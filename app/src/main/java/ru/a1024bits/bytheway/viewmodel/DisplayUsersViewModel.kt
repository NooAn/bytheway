package ru.a1024bits.bytheway.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import ru.a1024bits.bytheway.App.Companion.INSTANCE
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.model.Response
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.Filter
import ru.a1024bits.bytheway.repository.MAX_AGE
import ru.a1024bits.bytheway.repository.UserRepository
import ru.a1024bits.bytheway.util.Constants.FIRST_INDEX_CITY
import ru.a1024bits.bytheway.util.Constants.LAST_INDEX_CITY
import java.util.*
import javax.inject.Inject

/**
 * Created by andrey.gusenkov on 25/09/2017.
 */
class DisplayUsersViewModel @Inject constructor(var userRepository: UserRepository) : BaseViewModel() {
    var response: MutableLiveData<Response<List<User>>> = MutableLiveData()
    var yearsOldUsers = (0..MAX_AGE).mapTo(ArrayList<String>()) { it.toString() }
    val filter = Filter()

    companion object {
        const val TAG = "showUserViewModel"
    }

    fun getAllUsers(filter: Filter) {
        disposables.add(userRepository.getAllUsers()
                .subscribeOn(getBackgroundScheduler())
                .timeout(TIMEOUT_SECONDS, timeoutUnit)
                .retry(2)
                .doOnSubscribe({ loadingStatus.postValue(true) })
                .doAfterTerminate({ loadingStatus.postValue(false) })
                .doAfterSuccess { resultUsers -> filterUsersByFilter(resultUsers, filter) }
                .observeOn(getMainThreadScheduler())
                .subscribe(
                        { resultUsers ->
                            Log.e("LOG subscribe", Thread.currentThread().name)
                            response.postValue(Response.success(resultUsers))
                        },
                        { throwable -> response.postValue(Response.error(throwable)) }
                )
        )
    }

    fun sendUserData(map: HashMap<String, Any>, id: String) {
        loadingStatus.setValue(true)
        disposables.add(userRepository.changeUserProfile(map, id)
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

    fun getUsersWithSimilarTravel(paramSearch: Filter) {
        loadingStatus.setValue(true)
        disposables.add(userRepository.getReallUsers(paramSearch)
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

    fun getTextFromDates(date: Long?): String {
        val calendarStartDate = Calendar.getInstance()
        calendarStartDate.timeInMillis = date ?: 0L
        return getTextDateDayAndMonth(calendarStartDate)
    }

    fun getTextFromDates(startDate: Long?, endDate: Long?): String {
        val toWord = " - "

        val calendarStartDate = Calendar.getInstance()
        calendarStartDate.timeInMillis = startDate ?: 0L
        val calendarEndDate = Calendar.getInstance()
        calendarEndDate.timeInMillis = endDate ?: 0L

        if (calendarEndDate.timeInMillis == 0L)
            return getTextDateDayAndMonth(calendarStartDate)

        if (calendarStartDate.timeInMillis == 0L)
            return getTextDateDayAndMonth(calendarEndDate)

        var yearStart = ""
        var yearEnd = ""
        if (calendarStartDate.get(Calendar.YEAR) != calendarEndDate.get(Calendar.YEAR)) {
            yearStart = calendarStartDate.get(Calendar.YEAR).toString()
            yearEnd = calendarEndDate.get(Calendar.YEAR).toString()
        }
        return getTextDate(calendarStartDate, yearStart) + toWord + getTextDate(calendarEndDate, yearEnd)
    }

    fun filterUsersByString(queryCustomRegister: String, primaryQuery: String, primaryList: MutableList<User>): MutableList<User> =
            primaryList.filterTo(ArrayList()) {
                it.cities.filterValues { it1 -> it1.toLowerCase().contains(queryCustomRegister) }.isNotEmpty() || it.name.toLowerCase().contains(queryCustomRegister) ||
                        it.email.toLowerCase().contains(queryCustomRegister) || it.age.toString().contains(queryCustomRegister) ||
                        it.budget.toString().contains(queryCustomRegister) || it.lastName.toLowerCase().contains(queryCustomRegister) ||
                        it.phone.contains(primaryQuery) || it.route.contains(queryCustomRegister)
            }

    private fun getTextDate(calendarStartDate: Calendar, yearStart: String): String {
        return StringBuilder("").append(getTextDateDayAndMonth(calendarStartDate))
                .append(" ")
                .append(yearStart)
                .toString()
    }

    private fun getTextDateDayAndMonth(calendarStartDate: Calendar): String {
        return StringBuilder("").append(calendarStartDate.get(Calendar.DAY_OF_MONTH))
                .append(" ")
                .append(INSTANCE.applicationContext.resources.getStringArray(R.array.months_array)[calendarStartDate.get(Calendar.MONTH)])
                .toString()
    }

    private fun filterUsersByFilter(resultUsers: MutableList<User>, filter: Filter) {
        Log.e("LOG filter", Thread.currentThread().name)
        resultUsers.retainAll {
            (!((filter.startBudget >= 0) && (filter.endBudget > 0)) || (it.budget >= filter.startBudget && it.budget <= filter.endBudget)) &&
                    (!((filter.startDate > 0L) && (filter.endDate > 0L)) ||
                            ((it.dates["start_date"] ?: filter.startDate) >= filter.startDate &&
                                    (it.dates["end_date"] ?: filter.endDate) <= filter.endDate)) &&
                    ((it.age >= filter.startAge && it.age <= filter.endAge)) &&
                    ((filter.sex == 0) || (it.sex == filter.sex)) &&
                    ((filter.startCity.isEmpty()) || (it.cities[FIRST_INDEX_CITY]?.contains(filter.startCity, true) == true)) &&
                    ((filter.endCity.isEmpty()) || (it.cities[LAST_INDEX_CITY]?.contains(filter.endCity, true) == true))
        }
    }
}