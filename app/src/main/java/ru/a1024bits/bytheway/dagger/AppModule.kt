package ru.a1024bits.bytheway.dagger

import android.content.Context
import dagger.Module
import dagger.Provides
import ru.a1024bits.bytheway.App
import javax.inject.Singleton

/**
 * Created by andrey.gusenkov on 19/09/2017.
 */
@Module
class AppModule(val application: App) {
    
    @Provides
    @Singleton
    fun providerApplicationContext(): Context = application.applicationContext
}