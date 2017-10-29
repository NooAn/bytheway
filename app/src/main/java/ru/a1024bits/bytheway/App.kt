package ru.a1024bits.bytheway

import android.app.Application
import ru.a1024bits.bytheway.dagger.*
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router

/**
 * Created by andrey.gusenkov on 19/09/2017.
 */
class App : Application() {
    companion object {
        lateinit var INSTANCE: App
        lateinit var component: AppComponent
    }

    private lateinit var cicerone: Cicerone<Router>

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        component = DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .networkModule(NetworkModule())
                .navigationModule(NavigationModule())
                .userRepositoryModule(UserRepositoryModule())
                .build();

        initCicerone()

    }

    private fun initCicerone() {
        cicerone = Cicerone.create()
    }

    val navigatorHolder: NavigatorHolder
        get() = cicerone.navigatorHolder

    val router: Router
        get() = cicerone.router

    fun appComponent(): AppComponent = component

}