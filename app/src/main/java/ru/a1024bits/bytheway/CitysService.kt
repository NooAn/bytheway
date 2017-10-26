package ru.a1024bits.aviaanimation.ui

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query
import ru.a1024bits.bytheway.model.AviaCity
import java.util.*

/**
 * Created by x220 on 01.10.2017.
 * url for project : https://places.aviasales.ru/v2/places.json?term=Москва&locale=ru&types[]=airport&types[]=city
 */
interface CitysService {
    companion object {
        const val URL: String = "https://places.aviasales.ru/v2/"
    }

    @GET("/places.json")
    fun getCities(@Query("term") term: String,
                  @Query("locale") locale: String = "ru",
                  @Query("types[]") types: String
    ): Observable<List<AviaCity>>
}