package ru.a1024bits.bytheway.koin

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.gson.GsonBuilder
import org.koin.dsl.module.applicationContext
import io.reactivex.schedulers.Schedulers
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import ru.a1024bits.bytheway.MapWebService
import java.util.concurrent.TimeUnit

/**
 * Created by Andrei_Gusenkov on 3/26/2018.
 */
val remoteDatasourceModule = applicationContext {
    // provided web components
    bean { createOkHttpClient() }
    // Fill property
    bean { createWebService<MapWebService>(get(), SERVER_URL) }
}


const val TIMEOUT = 120L
const val SERVER_URL = "https://iappintheair.appspot.com"

fun createFirestore(): FirebaseFirestore {
    val store = FirebaseFirestore.getInstance()
    store.firestoreSettings = providesFirestoreSettings()
    return store
}

fun providesFirestoreSettings(): FirebaseFirestoreSettings = FirebaseFirestoreSettings.Builder()
        .setPersistenceEnabled(true)
        .setSslEnabled(true)
        .build()

fun createOkHttpClient(): OkHttpClient {
    val httpLoggingInterceptor = HttpLoggingInterceptor()
    httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
    return OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .build()
}

inline fun <reified T> createWebService(okHttpClient: OkHttpClient, url: String): T {
    val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .client(okHttpClient)
            .build()
    return retrofit.create(T::class.java)
}