package ru.a1024bits.bytheway.algorithm

import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.Filter
import ru.a1024bits.bytheway.util.Constants.END_DATE
import ru.a1024bits.bytheway.util.Constants.START_DATE

/**
 * Created by Bit on 12/16/2017.
 */
class SearchTravelers(val filter: Filter = Filter(), val user: User) {
    val WeightRoute: Int = 75
    val WeightBudget: Int = 8
    val WeightMethod: Int = 8
    val WeightDate: Int = 9

    fun getEstimation(): Int {
        return ((calculateRoute() * WeightRoute
                + calculateDate() * WeightDate
                + calculateMethod() * WeightMethod
                + calculateBudget() * WeightBudget) * 100).toInt()
    }

    fun calculateBudget(): Double {
        return ((1 / (Math.abs(user.budget - filter.endBudget) + 1)).toDouble())
    }

    fun calculateDate(): Double {
        val start = user.dates.get(START_DATE)?.toLong() ?: 0
        val end = user.dates.get(END_DATE)?.toLong() ?: 0
        if (filter.startDate == start && filter.endDate == end) return 1.0
        if (filter.startDate > start && filter.startDate > end && filter.endDate > start && filter.startDate > end) return 0.0
        if (filter.startDate < start && filter.endDate < end && filter.endDate < start && filter.startDate < end) return 0.0
        return ((1000.0 / Math.abs(filter.startDate.toDouble() - start.toDouble()))
                + (1000.0 / Math.abs(filter.endDate.toDouble() - end.toDouble()))) / 2
    }

    fun calculateMethod(): Int {
        return 0
    }

    fun calculateRoute(): Int {
        return 0
    }
}
