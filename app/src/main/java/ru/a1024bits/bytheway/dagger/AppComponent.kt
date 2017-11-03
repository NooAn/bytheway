package ru.a1024bits.bytheway.dagger

import dagger.Component
import ru.a1024bits.bytheway.ui.activity.MenuActivity
import ru.a1024bits.bytheway.ui.fragments.AllUsersFragment
import ru.a1024bits.bytheway.ui.fragments.MyProfileFragment
import ru.a1024bits.bytheway.viewmodel.ShowUsersViewModel
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
    fun inject(activity: MenuActivity)
    fun inject(fragment: AllUsersFragment)
    fun inject(mainViewModel: ShowUsersViewModel)
    fun inject(myProfileFragment: MyProfileFragment) {}

}