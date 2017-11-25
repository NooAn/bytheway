package ru.a1024bits.bytheway.dagger

import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton
import android.text.TextUtils
import com.squareup.okhttp.Credentials
import okhttp3.OkHttpClient
import retrofit2.converter.gson.GsonConverterFactory
import ru.a1024bits.bytheway.util.AuthenticationInterceptor


/**
 * Created by andrey.gusenkov on 19/09/2017.
 */
@Module
class NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
                .baseUrl("https://iappintheair.appspot.com")
                .build()
    }
}
