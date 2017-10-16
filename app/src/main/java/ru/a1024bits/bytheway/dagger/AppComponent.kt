package ru.a1024bits.bytheway.dagger

import dagger.Component
import ru.a1024bits.bytheway.ui.activity.MainActivity
import javax.inject.Singleton

/**
 * Created by andrey.gusenkov on 19/09/2017.
 */

@Singleton
@Component(modules = arrayOf(AppModule::class,
        NetworkModule::class,
        NavigationModule::class,
        UserRepositoryModule::class))

interface AppComponent {
    
    fun inject(activity: MainActivity);
    
}