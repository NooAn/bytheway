package ru.a1024bits.bytheway.dagger

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import ru.a1024bits.bytheway.viewmodel.*

/**
 * Created by andrey.gusenkov on 03/11/2017
 */
@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(DisplayUsersViewModel::class)
    // Bind your View Model here
    abstract fun bindShowViewModel(mainViewModel: DisplayUsersViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(MyProfileViewModel::class)
    abstract fun bindMyProfileViewModel(mainViewModel: MyProfileViewModel): ViewModel
    
    @Binds
    @IntoMap
    @ViewModelKey(RegistrationViewModel::class)
    abstract fun bindRegistrationViewModel(mainViewModel: RegistrationViewModel): ViewModel



    @Binds
    @IntoMap
    @ViewModelKey(UserProfileViewModel::class)
    abstract fun bindUserViewModel(mainViewModel: UserProfileViewModel): ViewModel


    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

}