package ru.a1024bits.bytheway.algorithm

import android.location.Location
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import ru.a1024bits.bytheway.model.Method
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
    fun getWeightMethod_Test_DateNullAll() {
        filter.startDate = 0
        filter.endDate = 0
        Assert.assertEquals(0.0, search.calculateDate(), 0.09)
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
    fun getMethod_equals_all() {
        filter.method = hashMapOf(Method.BUS.link to true,
                Method.TRAIN.link to true,
                Method.PLANE.link to true,
                Method.CAR.link to true,
                Method.HITCHHIKING.link to true)

        user.method = hashMapOf(Method.BUS.link to true,
                Method.TRAIN.link to true,
                Method.PLANE.link to true,
                Method.CAR.link to true,
                Method.HITCHHIKING.link to true)
        Assert.assertEquals(search.calculateMethod(), 1.0, 0.01)
    }

    @Test
    fun getMethod_Not_equals() {
        filter.method = hashMapOf(Method.BUS.link to false,
                Method.TRAIN.link to true,
                Method.PLANE.link to false,
                Method.CAR.link to false,
                Method.HITCHHIKING.link to true)

        user.method = hashMapOf(Method.BUS.link to true,
                Method.TRAIN.link to false,
                Method.PLANE.link to true,
                Method.CAR.link to false,
                Method.HITCHHIKING.link to false)

        Assert.assertEquals(search.calculateMethod(), 0.00, 0.00)
    }

    @Test
    fun getMethod_equals_2_from_2() {
        filter.method = hashMapOf(Method.BUS.link to true,
                Method.TRAIN.link to true,
                Method.PLANE.link to false,
                Method.CAR.link to false,
                Method.HITCHHIKING.link to false)

        user.method = hashMapOf(Method.BUS.link to true,
                Method.TRAIN.link to true,
                Method.PLANE.link to false,
                Method.CAR.link to false,
                Method.HITCHHIKING.link to false)
        Assert.assertEquals(search.calculateMethod(), 1.0, 0.01)
    }

    @Test
    fun getMethod_equals_1_from_4() {
        filter.method = hashMapOf(Method.BUS.link to true,
                Method.TRAIN.link to true,
                Method.PLANE.link to false,
                Method.CAR.link to true,
                Method.HITCHHIKING.link to true)

        user.method = hashMapOf(Method.BUS.link to true,
                Method.TRAIN.link to false,
                Method.PLANE.link to true,
                Method.CAR.link to false,
                Method.HITCHHIKING.link to false)

        Assert.assertEquals(search.calculateMethod(), 0.25, 0.01)
    }

    @Test
    fun getMethod_equals_3_for_5() {
        filter.method = hashMapOf(Method.BUS.link to false,
                Method.TRAIN.link to true,
                Method.PLANE.link to false,
                Method.CAR.link to false,
                Method.HITCHHIKING.link to true)

        user.method = hashMapOf(Method.BUS.link to true,
                Method.TRAIN.link to false,
                Method.PLANE.link to true,
                Method.CAR.link to false,
                Method.HITCHHIKING.link to true)

        Assert.assertEquals(search.calculateMethod(), 0.33, 0.01)
    }

    @Test
    fun getRoute_all_equals() {
        filter.locationEndCity = LatLng(5.5, 15.0)
        filter.locationStartCity = LatLng(2.5, 25.00)
        user.cityFromLatLng = GeoPoint(2.5, 25.00)
        user.cityToLatLng = GeoPoint(5.5, 15.0)
        Assert.assertEquals(search.calculateRoute(), 1.0, 0.01)
    }

    @Test
    fun getRoute_distance_start_11km_R_0() {
        // filter
        var latStart = 5.5
        val lonStart = 15.00
        val latEnd = 10.00
        val lonEnd = 10.00
        //user location
        val latStartUser = 5.546454
        val lonStartUSer = 14.909738

        filter.locationStartCity = LatLng(latStart, lonStart)
        filter.locationEndCity = LatLng(latEnd, lonEnd)

        user.cityFromLatLng = GeoPoint(latStartUser, lonStartUSer)
        user.cityToLatLng = GeoPoint(latEnd, lonEnd)

        Assert.assertEquals(search.calculateRoute(), 1.0, 0.01)
    }

    @Test
    fun getRoute_distance_start_25km() {
        // filter
        var latStart = 5.5
        val lonStart = 15.00
        val latEnd = 10.00
        val lonEnd = 10.00
        //user location
        val latStartUser = 5.622129
        val lonStartUSer = 14.809831

        filter.locationStartCity = LatLng(latStart, lonStart)
        filter.locationEndCity = LatLng(latEnd, lonEnd)
        user.cityFromLatLng = GeoPoint(latStartUser, lonStartUSer)
        user.cityToLatLng = GeoPoint(latEnd, lonEnd)
        Assert.assertEquals(0.920, search.calculateRoute(), 0.001)
    }

    @Test
    fun getEstimation_Test() {
        search.getEstimation()
    }

}