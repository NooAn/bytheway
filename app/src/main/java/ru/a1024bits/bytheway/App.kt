package ru.a1024bits.bytheway

import android.app.Application
import ru.a1024bits.bytheway.dagger.*

//import ru.a1024bits.bytheway.dagger.DaggerAppComponent

/**
 * Created by andrey.gusenkov on 19/09/2017.
 */
class App : Application() {
    companion object {
        lateinit var component: AppComponent
    }

//    val component: AppComponent by lazy {
//        DaggerAppComponent
//                .builder()
//                //.appModule(AppModule(this))
//                .build()
//    }
    
    override fun onCreate() {
        super.onCreate()
        component = DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .networkModule(NetworkModule())
                .userRepositoryModule(UserRepositoryModule())
                .build();
    }
    
     fun appComponent(): AppComponent = component
}