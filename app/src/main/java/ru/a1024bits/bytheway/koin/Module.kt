package ru.a1024bits.bytheway.koin

import org.koin.android.architecture.ext.viewModel
import org.koin.dsl.module.Module
import org.koin.dsl.module.applicationContext
import ru.a1024bits.bytheway.repository.IUsersRepository
import ru.a1024bits.bytheway.repository.UserRepository
import ru.a1024bits.bytheway.router.LocalCiceroneHolder
import ru.a1024bits.bytheway.viewmodel.*
import ru.terrakok.cicerone.Cicerone


/**
 * Created by Andrei_Gusenkov on 3/26/2018.
 */

// Koin module
val mainModule: Module = applicationContext {
    viewModel { UserProfileViewModel(get()) }
    viewModel { MyProfileViewModel(get()) }
    viewModel { DisplayUsersViewModel(get()) }
    viewModel { RegistrationViewModel(get()) }
    bean { LocalCiceroneHolder() }
    bean { Cicerone.create().navigatorHolder }
    bean { UserRepository(get(), get()) as IUsersRepository }
    bean { createFirestore() }
}

