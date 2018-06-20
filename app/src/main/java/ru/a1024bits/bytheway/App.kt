package ru.a1024bits.bytheway

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.squareup.leakcanary.LeakCanary
import com.vk.sdk.VKSdk
import io.fabric.sdk.android.Fabric
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
        VKSdk.initialize(this);
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
        Fabric.with(this, Crashlytics())
        Fabric.with(Fabric.Builder(this).kits(Crashlytics()).debuggable(true).build())
    }

    private fun initCicerone() {
        cicerone = Cicerone.create()
    }


}