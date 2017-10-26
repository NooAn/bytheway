package ru.a1024bits.bytheway.dagger

import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Created by andrey.gusenkov on 19/09/2017.
 */
@Module
class NetworkModule {
    
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
                .baseUrl("https://fucking")
                .build()
    }
}