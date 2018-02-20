package ru.a1024bits.bytheway

import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.*
import ru.a1024bits.bytheway.model.map_directions.RoutesList

/**
 * Created by tikhon.osipov on 26.11.17
 */
interface MapWebService {

    @GET("https://maps.googleapis.com/maps/api/directions/json")
    fun getDirections(@QueryMap queryMap: Map<String, String>): Call<RoutesList>

    @GET("https://maps.googleapis.com/maps/api/directions/json")
    fun getDirection(@QueryMap queryMap: Map<String, String>): Single<RoutesList>

    @POST("https://bytheway-f98ba.firebaseapp.com/sendnotifications")
    fun sendNotifications(@Body queryMap: Map<String, String>): Completable
}