package ru.a1024bits.bytheway;

import com.squareup.okhttp.ResponseBody
import io.reactivex.Single
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.model.AccessToken
import ru.a1024bits.bytheway.model.AirUser
import ru.a1024bits.bytheway.model.Airport


interface AirWebService {

    @GET("/oauth/token")
    fun getAccessToken(
            @Query("code") code: String,
            @Query("client_id") id: String,
            @Query("client_secret") clientSecret: String,
            @Query("grant_type") grantType: String,
            @Query("redirect_uri") uri: String): Call<AccessToken>

    @GET("/api/v1/me")
    fun getUserProfile(): Call<AirUser>

    @GET("/api/v1/me/trips")
    fun getMyTrips(): Call <AirUser>

    //https://yasen.hotellook.com/autocomplete?term=LED&lang=ru
    @GET("https://yasen.hotellook.com/autocomplete")
    fun getLatLngByCode(@Query("term") code: String,
                        @Query("lang") id: String): Single<Airport>

}