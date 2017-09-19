package ru.a1024bits.bytheway

import android.app.Application

/**
 * Created by andrey.gusenkov on 19/09/2017.
 */
class App : Application() {
//    companion object {
//        lateinit var component: AppComponent
//    }

//    val component: AppComponent by lazy {
//        AppComponent
//                .builder()
//                .appModule(AppModule(this))
//                .build()
//    }
    
    override fun onCreate() {
        super.onCreate()
//        component = AppComponent.builder().appModule(AppModule(this))
//                .build();
        //  component.inject(this)
    }
    
    // fun appComponent(): AppComponent = component
}