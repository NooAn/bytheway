package ru.a1024bits.bytheway.util

import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Andrei_Gusenkov on 1/23/2018
 */


class DateUtils {

    companion object {
        fun getLongFromDate(day: Int, month: Int, year: Int): Long {
            val dateString = "$day ${month + 1} $year"
            val dateFormat = SimpleDateFormat("dd MM yyyy", Locale.US)
            val date = dateFormat.parse(dateString)
            return date.time
        }

        val onDateTouch = View.OnTouchListener { view, event ->
            val drawableRight = 2
            (view as TextView)
            if (event.action == MotionEvent.ACTION_UP && view.compoundDrawables[drawableRight] != null) {
                if (event.rawX >= (view.right - view.compoundDrawables[drawableRight].bounds.width())) {
                    view.text = "  "
                }
            }
            false
        }
    }
}