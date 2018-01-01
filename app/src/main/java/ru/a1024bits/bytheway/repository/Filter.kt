package ru.a1024bits.bytheway.repository

import com.google.android.gms.maps.model.LatLng
import java.io.Serializable


data class Filter(var startAge: Int = 0,
                  var endAge: Int = -1,
                  var startBudget: Int = -1,
                  var endBudget: Int = -1,
                  var startCity: String = "",
                  var endCity: String = "",
                  var locationStartCity: LatLng = LatLng(0.0, 0.0),
                  var locationEndCity: LatLng = LatLng(0.0, 0.0),
                  var method: HashMap<String, Boolean> = hashMapOf<String, Boolean>(),
                  var sex: Int = 0,
                  var startDate: Long = 0L,
                  var endDate: Long = 0L) : Serializable

val M_SEX = 1
val W_SEX = 2
