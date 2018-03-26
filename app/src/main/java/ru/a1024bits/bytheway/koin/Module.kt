package ru.a1024bits.bytheway.koin

import com.google.firebase.firestore.FirebaseFirestore
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.android.get
import org.koin.dsl.module.Module
import org.koin.dsl.module.applicationContext
import ru.a1024bits.bytheway.repository.IUsersRepository
import ru.a1024bits.bytheway.repository.UserRepository
import ru.a1024bits.bytheway.viewmodel.MyProfileViewModel
import ru.a1024bits.bytheway.viewmodel.UserProfileViewModel

/**
 * Created by Andrei_Gusenkov on 3/26/2018.
 */

// Koin module
val mainModule: Module = applicationContext {
    viewModel { UserProfileViewModel(get()) }
    bean { UserRepository(get(), get()) as IUsersRepository }
    bean { FirebaseFirestore.getInstance() }
}

