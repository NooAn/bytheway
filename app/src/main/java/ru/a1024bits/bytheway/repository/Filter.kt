package ru.a1024bits.bytheway.repository


data class Filter(var startAge: Int = 0,
                  var endAge: Int = 0,
                  var startBudget: Int = 0,
                  var endBudget: Int = 0,
                  var startCity: String = "",
                  var endCity: String = "",
                  var sex: Int = 0,
                  var startDate: Long = 0,
                  var endDate: Long = 0)