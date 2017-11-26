package ru.a1024bits.bytheway.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.UserRepository
import javax.inject.Inject

/**
 * Created by tikhon.osipov on 26.11.17
 */
class MapViewModel @Inject constructor(var userRepository: UserRepository) : ViewModel() {
    val user: MutableLiveData<User> = MutableLiveData<User>()
    val load: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    val error: MutableLiveData<Int> = MutableLiveData<Int>()



    fun sendUserData(map: HashMap<String, Any>, id: String) {
        userRepository.changeUserProfile(map, id)
                .addOnCompleteListener {
                    //fixme
                    Log.e("LOG", "${this::class.java.simpleName}: complete user: complete? ${it.isComplete}; successful? ${it.isSuccessful}")
                }
                .addOnFailureListener {
                    Log.e("LOG", "${this::class.java.simpleName}: fail user")
                    //fixme Здесь обработка лоадера и показь пользователю ошибку загрузки ну не здеь а во вью. пример как эт осделать смотри в вью моделаър
                }
                .addOnSuccessListener {
                    Log.e("LOG", "${this::class.java.simpleName}: ok send user")
                    //fixme
                }
    }
}