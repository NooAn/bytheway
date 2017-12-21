package ru.a1024bits.bytheway.algorithm

import ru.a1024bits.bytheway.model.Method
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.Filter
import ru.a1024bits.bytheway.util.Constants.END_DATE
import ru.a1024bits.bytheway.util.Constants.START_DATE
import kotlin.math.max

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

    fun calculateMethod(): Double {
        var maxCountUser = 0
        var maxCountFilter = 0
        var p = 0
        for (value in Method.values()) {
            if (filter.method.get(value.link) == true) ++maxCountFilter
            if (user.method.get(value.link) == true) ++maxCountUser
            if (user.method.get(value.link) == filter.method.get(value.link) && filter.method.get(value.link) == true) ++p
        }
        maxCountUser = max(maxCountFilter, maxCountUser)
        val n = p * (1.0 / maxCountUser.toDouble())
        return n
    }

    fun fibbonaci(n: Int): Int {
        var prev: Long = 0
        var next: Long = 1
        var result: Long = 0
        for (i in 0 until n) {
            result = prev + next
            prev = next
            next = result
        }
        return result.toInt()
    }

    /*
    (x - x0)^2 + (y - y0)^2 <= R^2
    где x и y - координаты вашей точки,
    x0 и y0 - координаты центра окружности, R - радиус окружности, ^2 - возведение в квадрат.
    Если условие выполняется, то точка находится внутри (или на окружности,
    в случае равенства левой и правой частей). Если не выполняется,то пользователь вне окружности и значит расширяем радиус по фибоначчи
    */
    fun calculateRoute(): Double {
        val startPoint = (user.cityFromLatLng.latitude - filter.locationStartCity.latitude) * (user.cityFromLatLng.latitude - filter.locationStartCity.latitude)
        +((user.cityFromLatLng.longitude - filter.locationStartCity.longitude) * (user.cityFromLatLng.longitude - filter.locationStartCity.longitude))
        val endPoint = (user.cityToLatLng.latitude - filter.locationEndCity.latitude) * (user.cityToLatLng.latitude - filter.locationEndCity.latitude)
        +((user.cityToLatLng.longitude - filter.locationEndCity.longitude) * (user.cityToLatLng.longitude - filter.locationEndCity.longitude))
        var R = 0
        var indexFirst = 0.0
        var indexLast = 0.0
        for (i in 7..20) {
            R = fibbonaci(i)
            indexFirst = (71.2 - (136.0 * R)) / 0.4
            if (startPoint <= R * R) break
        }
        for (i in 7..20) {
            R = fibbonaci(i)
            indexLast = (71.2 - (136.0 * R)) / 0.4
            if (endPoint <= R * R) break
        }
        return indexFirst + indexLast
    }
}