package ru.a1024bits.bytheway;

import com.squareup.okhttp.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.model.AccessToken


interface AirWebService {

    @POST("/login")
    fun basicLogin(): Call<ResponseBody>

    @FormUrlEncoded
    @POST("/token")
    fun getAccessToken(
            @Field("code") code: String,
            @Field("grant_type") grantType: String): Call<AccessToken>

    @GET("/users")
    fun getUserProfile(@Query("fromCount") fromCount: Long, @Query("count") count: Int = 20): List<User>


}