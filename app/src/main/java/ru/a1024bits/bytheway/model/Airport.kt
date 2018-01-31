package ru.a1024bits.bytheway.model

import com.google.android.gms.maps.model.LatLng

/**
 * Created by Andrei_Gusenkov on 1/31/2018.
 */

data class Airport(var cities: List<City>)

data class City(var countryCode: String,
                var iata: List<String>,
                var location: LatLng)