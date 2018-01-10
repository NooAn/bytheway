package ru.a1024bits.bytheway.util

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Created by x220 on 25.11.2017.
 */
class AuthenticationInterceptor(private val authToken: String) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val builder = original.newBuilder()
                .header("Authorization", authToken)

        val request = builder.build()
        return chain.proceed(request)
    }
}