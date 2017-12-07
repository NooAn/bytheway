package ru.a1024bits.bytheway

import android.content.Context
import android.util.Log
import kotlinx.android.synthetic.main.searching_parameters_block.*
import java.util.*
import kotlin.collections.ArrayList


class ExtensionsAllUsers(val context: Context) {
    var yearsOldUsers = (0..120).mapTo(ArrayList<String>()) { it.toString() }

    fun getTextFromDates(startDate: Long, endDate: Long): String {
        return if (startDate > 0L && endDate > 0L) {
            val calendarStartDate = Calendar.getInstance()
            calendarStartDate.timeInMillis = startDate
            val calendarEndDate = Calendar.getInstance()
            calendarEndDate.timeInMillis = endDate
            var yearStart = ""
            var yearEnd = ""
            if (calendarStartDate.get(Calendar.YEAR) != calendarEndDate.get(Calendar.YEAR)) {
                yearStart = calendarStartDate.get(Calendar.YEAR).toString() + " "
                yearEnd = calendarEndDate.get(Calendar.YEAR).toString() + " "
            }
            StringBuilder("c   ").append(yearStart).append(calendarStartDate.get(Calendar.DAY_OF_MONTH)).append(" ")
                    .append(context.resources.getStringArray(R.array.months_array)[calendarStartDate.get(Calendar.MONTH)])
                    .append("   по   ").append(yearEnd).append(calendarEndDate.get(Calendar.DAY_OF_MONTH)).append(" ")
                    .append(context.resources.getStringArray(R.array.months_array)[calendarEndDate.get(Calendar.MONTH)]).toString()

        } else ""
    }
}