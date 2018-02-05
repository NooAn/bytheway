package ru.a1024bits.bytheway.model

import com.google.gson.annotations.SerializedName

/**
 * Created by x220 on 26.11.2017.
 */
data class AirUser(val error: String = "0", val status: String = "ok", val data: Data)

data class Data(val id: String, @SerializedName("last_year_kilometers") val lastYearKm: String,
                val flights: List<Fligths>, val hours: String,
                val airports: List<Airports>,
                val trips: List<Trips>,
                val name: String, val kilometers: String)

data class Place(val country: String, val country_full: String, val name: String, val code: String)

data class Trips(val id: String, val flights: List<Fligths>)

data class Fligths(
        @SerializedName("arrival_utc") val arrivalUtc: String,
        val number: String,
        @SerializedName("arrival_code") val arrivalCode: String,
        @SerializedName("departure_code") val departureCode: String,
        @SerializedName("departure_utc") val departureUtc: String,
        val origin: Place,
        @SerializedName("departure_locale") val departureLocale: Long,
        @SerializedName("arrival_locale") val arrivalLocale: Long,
        val destination: Place)

data class Airports(val count: String, val city: String, val country: String)