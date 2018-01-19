package ru.a1024bits.bytheway

import android.app.Application
import android.location.Location
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.squareup.leakcanary.LeakCanary
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
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);

//        var latStart = 5.5
//        val lonStart = 15.00
//        val latEnd = 10.00
//        val lonEnd = 10.00
//        //user location
//        val latStartUser = 5.546454
//        val lonStartUSer = 14.909738
//        val latEndUser = 8.0
//        val lonEndUser = 15.00
//        var location = Location("GPS")
//        location.latitude = latStart
//        location.longitude = lonStart
//        var locationEnd = Location("GPS2")
//        locationEnd.latitude = latStartUser
//        locationEnd.longitude = lonStartUSer
//        var locationFinish = Location("END")
//        locationFinish.latitude = latEnd
//        locationFinish.longitude = lonEnd
//        var locationFinisjUser = Location("GPS user end")
//        locationFinisjUser.latitude = latEndUser
//        locationFinisjUser.longitude = lonEndUser
//        Log.e("LOG", location.distanceTo(locationEnd).toString())
//
//        Log.e("LOG", locationFinish.distanceTo(locationFinisjUser).toString())

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