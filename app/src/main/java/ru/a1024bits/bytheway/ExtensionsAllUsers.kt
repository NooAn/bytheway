package ru.a1024bits.bytheway

import android.content.Context
import java.util.*
import kotlin.collections.ArrayList


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
        calendarStartDate.timeInMillis = startDate!!
        val calendarEndDate = Calendar.getInstance()
        calendarEndDate.timeInMillis = endDate!!
        var yearStart = ""
        var yearEnd = ""
        if (calendarStartDate.get(Calendar.YEAR) != calendarEndDate.get(Calendar.YEAR)) {
            yearStart = calendarStartDate.get(Calendar.YEAR).toString()
            yearEnd = calendarEndDate.get(Calendar.YEAR).toString()
        }
        return StringBuilder(fromWord).append(calendarStartDate.get(Calendar.DAY_OF_MONTH)).append(" ")
                .append(context.resources.getStringArray(R.array.months_array)[calendarStartDate.get(Calendar.MONTH)]).append(" ")
                .append(yearStart).append(toWord).append(calendarEndDate.get(Calendar.DAY_OF_MONTH)).append(" ")
                .append(context.resources.getStringArray(R.array.months_array)[calendarEndDate.get(Calendar.MONTH)]).append(" ")
                .append(yearEnd).toString()
    }
}