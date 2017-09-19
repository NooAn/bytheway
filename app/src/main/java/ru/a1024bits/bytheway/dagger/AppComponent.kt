package ru.a1024bits.bytheway.dagger

import dagger.Component
import ru.a1024bits.bytheway.App
import javax.inject.Singleton

/**
 * Created by andrey.gusenkov on 19/09/2017.
 */

@Singleton
@Component(modules = arrayOf(AppModule::class,
        NetworkModule::class,
        UserRepositoryModule::class))

interface AppComponent {
    //fun inject(userRepository: UserRepository)
    fun inject(app: App)
}