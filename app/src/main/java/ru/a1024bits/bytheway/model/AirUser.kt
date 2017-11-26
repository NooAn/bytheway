package ru.a1024bits.bytheway.model

import com.google.gson.annotations.SerializedName

/**
 * Created by x220 on 26.11.2017.
 */
data class AirUser(val error: String, val status: String, val data: Data)

data class Data(val id: String, @SerializedName("last_year_kilometers") val lastYearKm: String,
                val flights: List<Fligths>, val hours: String,
                val airports: List<Airports>,
                val name: String, val kilometers: String)

data class Fligths(val carrier: String,
                   @SerializedName("arrival_utc") val arrivalUtc: String,
                   val number: String,
                   @SerializedName("arrival_code") val arrivalCode: String,
                   @SerializedName("departure_code") val departureCode: String,
                   @SerializedName("departure_utc") val departureUtc: String)

data class Airports(val count: String, val city: String, val country: String)