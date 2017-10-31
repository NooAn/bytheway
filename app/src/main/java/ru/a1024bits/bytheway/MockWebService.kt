package ru.a1024bits.bytheway;

import retrofit2.http.GET
import retrofit2.http.Query
import ru.a1024bits.bytheway.model.User

interface MockWebService {

    @GET("/users")
    fun getChanUsers(@Query("fromCount") fromCount: Long, @Query("count") count: Int = 20): List<User>
}