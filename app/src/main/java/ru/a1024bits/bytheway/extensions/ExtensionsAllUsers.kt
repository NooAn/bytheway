package ru.a1024bits.bytheway.extensions

import android.content.Context
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.model.User
import java.util.*
import kotlin.collections.ArrayList

// FIXME memory leak
class ExtensionsAllUsers(val context: Context) {
    var yearsOldUsers = (0..120).mapTo(ArrayList<String>()) { it.toString() }

    fun getTextFromDates(startDate: Long?, endDate: Long?, variant: Int): String {
        var fromWord = context.getString(R.string.from_date_filter)
        var toWord = context.getString(R.string.to_date_filter)
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
                .append(context.resources.getStringArray(R.array.months_array)[calendarStartDate.get(Calendar.MONTH)]).append(" ")
                .append(yearStart).append(toWord).append(calendarEndDate.get(Calendar.DAY_OF_MONTH)).append(" ")
                .append(context.resources.getStringArray(R.array.months_array)[calendarEndDate.get(Calendar.MONTH)]).append(" ")
                .append(yearEnd).toString()
    }
}