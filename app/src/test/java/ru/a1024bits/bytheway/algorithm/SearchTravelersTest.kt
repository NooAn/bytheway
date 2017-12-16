package ru.a1024bits.bytheway.algorithm

import org.junit.Assert
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.Filter
import ru.a1024bits.bytheway.util.Constants.END_DATE
import ru.a1024bits.bytheway.util.Constants.START_DATE

/**
 * Created by Bit on 12/16/2017.
 */
class SearchTravelersTest {
    lateinit var search: SearchTravelers
    lateinit var filter: Filter
    lateinit var user: User
    @Before
    fun setUp() {
        filter = Filter()
        filter.endBudget = 300
        filter.endDate = 20_000
        filter.startDate = 15_000
        user = User()
        user.budget = 350
        user.dates.put(START_DATE, 14_000)
        user.dates.put(END_DATE, 16_000)
        search = SearchTravelers(filter, user)
    }

    @Test
    fun getWeightRoute_Test() {

    }

    @Test
    fun getWeightBudget_Test() {
        Assert.assertEquals(search.calculateBudget(), 0.019, 0.0999)
    }

    @Test
    fun getWeightMethod_Test_formula() {
        Assert.assertEquals(0.6250, search.calculateDate(), 0.09)
    }

    @Test
    fun getWeightMethod_Test_1_equals_dates() {
        filter.startDate = 14_000
        filter.endDate = 16_000
        Assert.assertEquals(search.calculateDate(), 1.0, 0.01)
    }

    @Test
    fun getWeightMethod_Test_0() {
        filter.startDate = 50_000
        filter.endDate = 51_000
        Assert.assertEquals(search.calculateDate(), 0.0, 0.01)
    }

    @Test
    fun getWeightMethod_Test_0_2() {
        filter.startDate = 100
        filter.endDate = 200
        Assert.assertEquals(search.calculateDate(), 0.0, 0.01)
    }

    @Test
    fun getWeightDate_Test() {

    }

    @Test
    fun getEstimation_Test() {

    }

}