package ru.a1024bits.bytheway

import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap
import ru.a1024bits.bytheway.model.Airport
import ru.a1024bits.bytheway.model.map_directions.RoutesList

/**
 * Created by tikhon.osipov on 26.11.17
 */
interface MapWebService {

    @GET("https://maps.googleapis.com/maps/api/directions/json")
    fun getDirections(@QueryMap queryMap: Map<String, String>): Call<RoutesList>

    @GET("https://maps.googleapis.com/maps/api/directions/json")
    fun getDirection(@QueryMap queryMap: Map<String, String>): Single<RoutesList>

}