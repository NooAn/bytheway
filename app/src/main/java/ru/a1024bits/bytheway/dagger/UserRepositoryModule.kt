package ru.a1024bits.bytheway.dagger

import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import ru.a1024bits.bytheway.repository.UserRepository
import javax.inject.Singleton

/**
 * Created by andrey.gusenkov on 19/09/2017.
 */
@Module
class UserRepositoryModule {
    
    @Provides
    @Singleton
    fun provideUserRepository(store: FirebaseFirestore): UserRepository = UserRepository(store)
    
    @Provides
    @Singleton
    fun providesFirestoreRepository(): FirebaseFirestore = FirebaseFirestore.getInstance()
}