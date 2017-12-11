package ru.a1024bits.bytheway.dagger

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
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
    fun providesFirestoreRepository(settings: FirebaseFirestoreSettings): FirebaseFirestore {
        val store = FirebaseFirestore.getInstance();
        store.firestoreSettings = settings
        return store
    }

    @Provides
    @Singleton
    fun providesFirestoreSettings(): FirebaseFirestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .setSslEnabled(true)
            .build();
}