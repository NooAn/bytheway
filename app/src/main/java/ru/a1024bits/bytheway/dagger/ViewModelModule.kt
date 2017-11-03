package ru.a1024bits.bytheway.dagger

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import ru.a1024bits.bytheway.viewmodel.ShowUsersViewModel
import ru.a1024bits.bytheway.viewmodel.ViewModelFactory

/**
 * Created by andrey.gusenkov on 03/11/2017.
 */
@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(ShowUsersViewModel::class)
    // Bind your View Model here
    abstract fun bindMainViewModel(mainViewModel: ShowUsersViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

}