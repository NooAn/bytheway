package ru.a1024bits.bytheway.dagger

import dagger.Module
import dagger.Provides
import ru.a1024bits.bytheway.router.LocalCiceroneHolder
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import javax.inject.Singleton


/**
 * Created by andrey.gusenkov on 12/10/2017.
 */
@Module
class NavigationModule {
    private val cicerone: Cicerone<Router>
    
    init {
        cicerone = Cicerone.create()
    }
    
    @Provides
    @Singleton
    internal fun provideRouter(): Router {
        return cicerone.router
    }
    
    @Provides
    @Singleton
    internal fun provideNavigatorHolder(): NavigatorHolder {
        return cicerone.navigatorHolder
    }
    
    @Provides
    @Singleton
    fun provideLocalNavigationHolder(): LocalCiceroneHolder {
        return LocalCiceroneHolder()
    }
}