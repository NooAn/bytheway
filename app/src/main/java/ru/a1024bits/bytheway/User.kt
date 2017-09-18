package ru.a1024bits.bytheway

import retrofit2.http.GET
import retrofit2.http.Path


/**
 * Created by andrey.gusenkov on 18/09/2017.
 */
data class User(val name: String, val lastName: String = "", val age: Int = 0)
