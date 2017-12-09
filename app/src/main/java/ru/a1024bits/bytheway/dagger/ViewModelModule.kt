package ru.a1024bits.bytheway.dagger

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import ru.a1024bits.bytheway.viewmodel.MapViewModel
import ru.a1024bits.bytheway.viewmodel.MyProfileViewModel
import ru.a1024bits.bytheway.viewmodel.DisplayUsersViewModel
import ru.a1024bits.bytheway.viewmodel.ViewModelFactory

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
    @ViewModelKey(MapViewModel::class)
    abstract fun bindMapViewModel(mainViewModel: MapViewModel): ViewModel
    
    
    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
    
}