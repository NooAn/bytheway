package ru.a1024bits.bytheway.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.borax12.materialdaterangepicker.date.DatePickerDialog
import io.reactivex.schedulers.Schedulers
import ru.a1024bits.bytheway.App.Companion.INSTANCE
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.model.Response
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.Filter
import ru.a1024bits.bytheway.repository.MAX_AGE
import ru.a1024bits.bytheway.repository.UserRepository
import ru.a1024bits.bytheway.ui.fragments.AllUsersFragment
import java.util.*
import javax.inject.Inject

/**
 * Created by andrey.gusenkov on 25/09/2017.
 */
class DisplayUsersViewModel @Inject constructor(var userRepository: UserRepository) : BaseViewModel() {
    val loadingStatus = MutableLiveData<Boolean>()
    var response: MutableLiveData<Response<List<User>>> = MutableLiveData()
    var yearsOldUsers = (0..MAX_AGE).mapTo(ArrayList<String>()) { it.toString() }
    val filter = Filter()

    val TAG = "showUserViewModel"

    fun getAllUsers(filter: Filter) {
        disposables.add(userRepository.getAllUsers()
                .subscribeOn(getBackgroundScheduler())
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
        //fixme
        userRepository.changeUserProfile(map, id).subscribe()
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

    fun updateDateDialog(fragment: AllUsersFragment): DatePickerDialog {
        val currentStartDate = Calendar.getInstance()
        if (filter.startDate > 0L) currentStartDate.timeInMillis = filter.startDate
        val currentEndDate = Calendar.getInstance()
        currentEndDate.timeInMillis = currentEndDate.timeInMillis + 1000L * 60 * 60 * 24
        if (filter.endDate > 0L) currentEndDate.timeInMillis = filter.endDate

        val dateDialog = DatePickerDialog.newInstance(
                { _, year, monthOfYear, dayOfMonth, yearEnd, monthOfYearEnd, dayOfMonthEnd ->
                    val calendarStartDate = Calendar.getInstance()
                    calendarStartDate.set(Calendar.YEAR, year)
                    calendarStartDate.set(Calendar.MONTH, monthOfYear)
                    calendarStartDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    val calendarEndDate = Calendar.getInstance()
                    calendarEndDate.set(Calendar.YEAR, yearEnd)
                    calendarEndDate.set(Calendar.MONTH, monthOfYearEnd)
                    calendarEndDate.set(Calendar.DAY_OF_MONTH, dayOfMonthEnd)

                    if (calendarStartDate.timeInMillis >= calendarEndDate.timeInMillis) {
                        fragment.nonSuchSetDate()
                        return@newInstance
                    }
                    filter.startDate = calendarStartDate.timeInMillis
                    filter.endDate = calendarEndDate.timeInMillis
                    fragment.suchSetDate()
                },
                currentStartDate.get(Calendar.YEAR),
                currentStartDate.get(Calendar.MONTH),
                currentStartDate.get(Calendar.DAY_OF_MONTH),
                currentEndDate.get(Calendar.YEAR),
                currentEndDate.get(Calendar.MONTH),
                currentEndDate.get(Calendar.DAY_OF_MONTH))
        dateDialog.setStartTitle(INSTANCE.applicationContext.resources.getString(R.string.date_start))
        dateDialog.setEndTitle(INSTANCE.applicationContext.resources.getString(R.string.date_end))
        return dateDialog
    }

    fun getTextFromDates(startDate: Long?, endDate: Long?, variant: Int): String {
        var fromWord = INSTANCE.applicationContext.getString(R.string.from_date_filter)
        var toWord = INSTANCE.applicationContext.getString(R.string.to_date_filter)
        if (variant == 1) {
            fromWord = ""
            toWord = " - "
        }
        val calendarStartDate = Calendar.getInstance()
        calendarStartDate.timeInMillis = startDate ?: 0L
        val calendarEndDate = Calendar.getInstance()
        calendarEndDate.timeInMillis = endDate ?: 0L
        var yearStart = ""
        var yearEnd = ""
        if (calendarStartDate.get(Calendar.YEAR) != calendarEndDate.get(Calendar.YEAR)) {
            yearStart = calendarStartDate.get(Calendar.YEAR).toString()
            yearEnd = calendarEndDate.get(Calendar.YEAR).toString()
        }
        return getTextStartEndDates(fromWord, calendarStartDate, yearStart, toWord, calendarEndDate, yearEnd)
    }

    fun filterUsersByString(queryCustomRegister: String, primaryQuery: String, primaryList: MutableList<User>): MutableList<User> =
            primaryList.filterTo(ArrayList()) {
                it.cities.filterValues { it1 -> it1.toLowerCase().contains(queryCustomRegister) }.isNotEmpty() || it.name.toLowerCase().contains(queryCustomRegister) ||
                        it.email.toLowerCase().contains(queryCustomRegister) || it.age.toString().contains(queryCustomRegister) ||
                        it.budget.toString().contains(queryCustomRegister) || it.lastName.toLowerCase().contains(queryCustomRegister) ||
                        it.phone.contains(primaryQuery) || it.route.contains(queryCustomRegister)
            }

    private fun getTextStartEndDates(fromWord: String?, calendarStartDate: Calendar, yearStart: String, toWord: String?, calendarEndDate: Calendar, yearEnd: String): String {
        return StringBuilder(fromWord).append(calendarStartDate.get(Calendar.DAY_OF_MONTH)).append(" ")
                .append(INSTANCE.applicationContext.resources.getStringArray(R.array.months_array)[calendarStartDate.get(Calendar.MONTH)]).append(" ")
                .append(yearStart).append(toWord).append(calendarEndDate.get(Calendar.DAY_OF_MONTH)).append(" ")
                .append(INSTANCE.applicationContext.resources.getStringArray(R.array.months_array)[calendarEndDate.get(Calendar.MONTH)]).append(" ")
                .append(yearEnd).toString()
    }

    private fun filterUsersByFilter(resultUsers: MutableList<User>, filter: Filter) {
        Log.e("LOG filter", Thread.currentThread().name)
        resultUsers.retainAll {
            (!((filter.startBudget >= 0) && (filter.endBudget > 0)) || (it.budget >= filter.startBudget && it.budget <= filter.endBudget)) &&
                    (!((filter.startDate > 0L) && (filter.endDate > 0L)) || ((it.dates["start_date"] as Long) >= filter.startDate && (it.dates["end_date"] as Long) <= filter.endDate)) &&
                    ((it.age >= filter.startAge && it.age <= filter.endAge)) &&
                    ((filter.sex == 0) || (it.sex == filter.sex)) &&
                    ((filter.startCity.isEmpty()) || (it.cities.contains(filter.startCity))) &&
                    ((filter.endCity.isEmpty()) || (it.cities.contains(filter.endCity)))
        }
    }
}