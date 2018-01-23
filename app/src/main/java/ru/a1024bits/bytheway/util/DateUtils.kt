package ru.a1024bits.bytheway.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Andrei_Gusenkov on 1/23/2018.
 */
fun getLongFromDate(day: Int, month: Int, year: Int): Long {
    val dateString = "$day ${month + 1} $year"
    val dateFormat = SimpleDateFormat("dd MM yyyy", Locale.US)
    val date = dateFormat.parse(dateString)
    return date.time
}