package ru.a1024bits.bytheway.repository

import java.io.Serializable


data class Filter(var startAge: Int = 0,
                  var endAge: Int = -1,
                  var startBudget: Int = -1,
                  var endBudget: Int = -1,
                  var startCity: String = "",
                  var endCity: String = "",
                  var sex: Int = 0,
                  var startDate: Long = 0L,
                  var endDate: Long = 0L) : Serializable