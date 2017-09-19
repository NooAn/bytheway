package ru.a1024bits.bytheway

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import ru.a1024bits.bytheway.model.User

/**
 * Created by andrey.gusenkov on 18/09/2017.
 */
interface WebService {
    
    @GET("/users/{user}")
    fun getUser(@Path("user") userId: String): Call<User>
    
}