package ru.a1024bits.bytheway.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.os.AsyncTask
import com.borax12.materialdaterangepicker.date.DatePickerDialog
import com.google.firebase.firestore.QuerySnapshot
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
    var usersLiveData: MutableLiveData<List<User>> = MutableLiveData<List<User>>()
    val loadingStatus = MutableLiveData<Boolean>()
    var response: MutableLiveData<Response<List<User>>> = MutableLiveData()
    var yearsOldUsers = (0..MAX_AGE).mapTo(ArrayList<String>()) { it.toString() }
    val filter = Filter()

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

    fun filterUsersInAdapterByString(queryCustomRegister: String, primaryQuery: String, primaryList: MutableList<User>): MutableList<User> =
            primaryList.filterTo(ArrayList()) {
                it.cities.containsValue(primaryQuery) || it.name.toLowerCase().contains(queryCustomRegister) || it.email.toLowerCase().contains(queryCustomRegister) ||
                        it.age.toString().contains(queryCustomRegister) || it.budget.toString().contains(queryCustomRegister) ||
                        it.city.toLowerCase().contains(queryCustomRegister) || it.lastName.toLowerCase().contains(queryCustomRegister) ||
                        it.phone.contains(queryCustomRegister) || it.route.contains(queryCustomRegister)

            }

    private fun getTextStartEndDates(fromWord: String?, calendarStartDate: Calendar, yearStart: String, toWord: String?, calendarEndDate: Calendar, yearEnd: String): String {
        return StringBuilder(fromWord).append(calendarStartDate.get(Calendar.DAY_OF_MONTH)).append(" ")
                .append(INSTANCE.applicationContext.resources.getStringArray(R.array.months_array)[calendarStartDate.get(Calendar.MONTH)]).append(" ")
                .append(yearStart).append(toWord).append(calendarEndDate.get(Calendar.DAY_OF_MONTH)).append(" ")
                .append(INSTANCE.applicationContext.resources.getStringArray(R.array.months_array)[calendarEndDate.get(Calendar.MONTH)]).append(" ")
                .append(yearEnd).toString()
    }
}