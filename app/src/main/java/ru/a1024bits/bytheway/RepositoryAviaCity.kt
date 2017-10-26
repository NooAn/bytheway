package ru.a1024bits.aviaanimation.ui

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory


/**
 * Created by x220 on 01.10.2017.
 */
class RepositoryAviaCity {
    companion object {
        fun create(): CitysService {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

            val retrofit = Retrofit.Builder()
                    .client(client)
                    .addCallAdapterFactory(
                            RxJava2CallAdapterFactory.create())
                    .addConverterFactory(
                            GsonConverterFactory.create())
                    .baseUrl(CitysService.URL)
                    .build()

            return retrofit.create(CitysService::class.java)
        }
    }
    fun getCities(term : String, types : String = "airport")  =  create().getCities(term, locale = "ru", types = types);
}