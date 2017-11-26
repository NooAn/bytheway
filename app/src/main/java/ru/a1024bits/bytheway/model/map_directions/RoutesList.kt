package ru.a1024bits.bytheway.model.map_directions

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by tikhon.osipov on 26.11.17
 */
data class RoutesList(
        @Expose @SerializedName("routes") val routes: List<Route>? = listOf()
)