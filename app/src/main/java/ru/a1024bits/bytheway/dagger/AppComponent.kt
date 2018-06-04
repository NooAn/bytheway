package ru.a1024bits.bytheway.dagger

import dagger.Component
import ru.a1024bits.bytheway.ui.activity.LoginActivity
import ru.a1024bits.bytheway.ui.activity.MenuActivity
import ru.a1024bits.bytheway.ui.activity.SplashActivity
import ru.a1024bits.bytheway.ui.dialogs.SocNetworkdialog
import ru.a1024bits.bytheway.ui.fragments.*
import ru.a1024bits.bytheway.viewmodel.DisplayUsersViewModel
import ru.a1024bits.bytheway.viewmodel.RegistrationViewModel
import javax.inject.Singleton

/**
 * Created by andrey.gusenkov on 19/09/2017.
 */

@Singleton
@Component(modules = arrayOf(AppModule::class,
        NetworkModule::class,
        NavigationModule::class,
        ViewModelModule::class,
        UserRepositoryModule::class))

interface AppComponent {
    fun inject(activity: LoginActivity)
    fun inject(activity: MenuActivity)
    fun inject(fragment: AllUsersFragment)
    fun inject(fragment: SimilarTravelsFragment)
    fun inject(mainViewModel: DisplayUsersViewModel)
    fun inject(myProfileFragment: MyProfileFragment)
    fun inject(fragment: RegistrationViewModel)
    fun inject(fragment: SearchFragment)
    fun inject(activity: SplashActivity)
    fun inject(mapFragment: MapFragment)
    fun inject(userProfileFragment: UserProfileFragment)
    fun inject(socNetworkdialog: SocNetworkdialog)
}