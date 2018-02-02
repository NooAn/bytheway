package ru.a1024bits.bytheway.util

import android.text.TextUtils
import android.util.Log
import com.squareup.okhttp.Credentials
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by x220 on 25.11.2017.
 */
const val API_BASE_URL = "https://iappintheair.appspot.com"

class ServiceGenerator {

    private val httpClient = OkHttpClient.Builder()

    private val builder = Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())

    private var retrofit = builder.build()

    fun <S> createService(serviceClass: Class<S>): S {
        return createService(serviceClass, null, null)
    }

    fun <S> createService(
            serviceClass: Class<S>, username: String?, password: String?): S {
        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
            val authToken = Credentials.basic(username, password)
            return createService(serviceClass, authToken)
        }

        return createService(serviceClass, "")
    }

    fun <S> createService(
            serviceClass: Class<S>, authToken: String): S {
        if (!TextUtils.isEmpty(authToken)) {
            val interceptor = AuthenticationInterceptor(authToken)
            if (!httpClient.interceptors().contains(interceptor)) {
                httpClient.addInterceptor(interceptor)
                httpClient.addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
                builder.client(httpClient.build())
                retrofit = builder.build()
            }
        }

        return retrofit.create(serviceClass)
    }
}