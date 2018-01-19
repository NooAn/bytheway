package ru.a1024bits.bytheway.repository

import com.google.android.gms.maps.model.LatLng
import ru.a1024bits.bytheway.model.Method
import java.io.Serializable


data class Filter(var startAge: Int = 0,
                  var endAge: Int = MAX_AGE,
                  var startBudget: Int = -1,
                  var endBudget: Int = -1,
                  var startCity: String = "",
                  var endCity: String = "",
                  var locationStartCity: LatLng = LatLng(0.0, 0.0),
                  var locationEndCity: LatLng = LatLng(0.0, 0.0),
                  var method: HashMap<String, Boolean> = hashMapOf(Method.BUS.link to false,
                          Method.TRAIN.link to false,
                          Method.PLANE.link to false,
                          Method.CAR.link to false,
                          Method.HITCHHIKING.link to false),
                  var sex: Int = 0,
                  var startDate: Long = 0L,
                  var endDate: Long = 0L) : Serializable

val M_SEX = 1
val W_SEX = 2
val MAX_AGE = 80
