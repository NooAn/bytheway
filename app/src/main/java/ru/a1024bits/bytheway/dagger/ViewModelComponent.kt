package ru.a1024bits.bytheway.dagger

import dagger.Component
import ru.a1024bits.bytheway.viewmodel.ShowUsersViewModel

/**
 * Created by andrey.gusenkov on 03/11/2017.
 */
@Component(modules = arrayOf(
        ViewModelModule::class
))
interface ViewModelComponent {
    // inject your view models
    fun inject(mainViewModel: ShowUsersViewModel)
    
}