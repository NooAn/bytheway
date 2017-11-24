package ru.a1024bits.bytheway

import android.content.Context
import android.util.Log
import java.util.*
import kotlin.collections.ArrayList


class ExtensionsAllUsers(val context: Context) {
    var yearsOldUsers = (0..120).mapTo(ArrayList<String>()) { it.toString() }
    private val calendarTempDate: Calendar = Calendar.getInstance()
    val currentDate: Calendar = Calendar.getInstance()
    var daysInMonths: ArrayList<Long>

    init {
        calendarTempDate.set(Calendar.DAY_OF_MONTH, calendarTempDate.get(Calendar.DAY_OF_MONTH) - 1)
        calendarTempDate.set(Calendar.HOUR_OF_DAY, 0)
        calendarTempDate.set(Calendar.MINUTE, 0)
        calendarTempDate.set(Calendar.SECOND, 0)
        calendarTempDate.set(Calendar.MILLISECOND, 0)
        daysInMonths = (0..122).mapTo(ArrayList()) {
            calendarTempDate.set(Calendar.DAY_OF_MONTH, calendarTempDate.get(Calendar.DAY_OF_MONTH) + 1)
            calendarTempDate.timeInMillis
        }
        calendarTempDate.time = Date()
    }

    fun getPositionFromDate(date: Long, isEndAge: Boolean): Int {
        if (date > 0L) {
            (0 until daysInMonths.size)
                    .filter { it -> daysInMonths[it] == date }
                    .forEach { it -> return it }
        }
        return if (isEndAge) daysInMonths.size - 1 else 0
    }

    fun getDaysInEndDataStr(): MutableList<String> {
        return (0 until daysInMonths.size).mapTo(ArrayList()) {
            calendarTempDate.time = Date(daysInMonths[it])
            calendarTempDate.get(Calendar.DAY_OF_MONTH).toString() + " " + context.resources.getStringArray(R.array.months_array)[calendarTempDate.get(Calendar.MONTH)]
        }
    }
}