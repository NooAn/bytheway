package ru.a1024bits.bytheway;

import com.squareup.okhttp.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.model.AccessToken


interface AirWebService {

    @GET("/oauth/token")
    fun getAccessToken(
            @Query("code") code: String,
            @Query("client_id") id: String,
            @Query("client_secret") clientSecret: String,
            @Query("grant_type") grantType: String,
            @Query("redirect_uri") uri: String): Call<AccessToken>

    @GET("/api/v1/me")
    fun getUserProfile(): List<User>


}