package ru.a1024bits.bytheway.dagger

import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import ru.a1024bits.bytheway.WebService
import ru.a1024bits.bytheway.repository.UserRepository
import javax.inject.Singleton

/**
 * Created by andrey.gusenkov on 19/09/2017.
 */
@Module
class UserRepositoryModule {
    
    @Provides
    @Singleton
    fun providesWebService(retrofit: Retrofit): WebService? = retrofit.create(WebService::class.java)
    
    @Provides
    @Singleton
    fun provideUserRepository(webService: WebService) : UserRepository = UserRepository(webService)
}