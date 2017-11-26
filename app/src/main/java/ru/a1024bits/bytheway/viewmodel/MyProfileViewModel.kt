package ru.a1024bits.bytheway.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.text.Editable
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import ru.a1024bits.bytheway.model.*
import ru.a1024bits.bytheway.repository.COLLECTION_USERS
import ru.a1024bits.bytheway.repository.UserRepository
import javax.inject.Inject

/**
 * Created by andrey.gusenkov on 25/09/2017.
 */
class MyProfileViewModel @Inject constructor(var userRepository: UserRepository) : ViewModel() {
    val user: MutableLiveData<User> = MutableLiveData<User>()
    val load: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    val error: MutableLiveData<Int> = MutableLiveData<Int>()

    fun load(userId: String) {
        Log.e("LOG", "start load user: $userId")
        userRepository.getUserById(userId)
                .addOnFailureListener {
                    Log.e("LOG", "error ${it.message}")
                }
                .addOnSuccessListener { document ->
                    val profile = document.toObject(User::class.java)
                    user.setValue(profile)
                }
        Log.e("LOG", "end load user: $userId")
    }

    fun saveLinks(arraySocNetwork: List<SocialNetwork>, id: String) {
        val map: HashMap<String, Any> = hashMapOf()
        // map.put("socialNetwork", arraySocNetwork) fixme
        userRepository.changeUserProfile(map, id)
                .addOnFailureListener {
                    error.value = 1
                    Log.e("LOG", "fail link change ${it.message}", it)
                }
                .addOnCompleteListener {
                    Log.e("LOG", "oncomplete link change")
                }
    }


    fun ifUserNotExistThenSave(currentUser: FirebaseUser?) {
        val store = FirebaseFirestore.getInstance()
        val docRef = store.collection(COLLECTION_USERS).document(currentUser?.uid.toString());
        docRef.get().addOnCompleteListener(object : OnCompleteListener<DocumentSnapshot> {
            override fun onComplete(task: Task<DocumentSnapshot>) {
                if (task.isSuccessful()) {
                    val document = task.getResult();
                    if (!document.exists()) {
                        // Пользователя нет в системе, добавляем.
                        userRepository.addUser(User().apply {
                            val list = currentUser?.displayName?.split(" ")
                            name = list?.get(0).orEmpty()
                            lastName = list?.get(1).orEmpty()
                            id = currentUser?.uid.orEmpty()
                            email = currentUser?.email.toString()
                            phone = currentUser?.phoneNumber.toString()
                            urlPhoto = currentUser?.photoUrl.toString()
                            currentUser?.getUid()
                        }).addOnCompleteListener {
                            load.value = true
                        }.addOnFailureListener({
                            load.value = false
                        })
                        Log.d("LOG", "No such document and create new doc");
                    } else {
                        // Пользователь уже существует и не нужно тогда добавлять его
                        Log.d("LOG", "DocumentSnapshot data: " + task.getResult().getData());
                        load.value = true
                    }
                } else {
                    load.value = false
                    Log.d("LOG", "get failed with ", task.getException());
                }
            }
        })
    }

    fun sendUserData(map: HashMap<String, Any>, id: String) {
        Log.e("LOG map:", map.toString())
        userRepository.changeUserProfile(map, id)
                .addOnCompleteListener {
                    //fixme
                    Log.e("LOG", "complete user")
                }
                .addOnFailureListener {
                    Log.e("LOG", "fail user")
                    //fixme Здесь обработка лоадера и показь пользователю ошибку загрузки ну не здеь а во вью. пример как эт осделать смотри в вью моделаър
                }
                .addOnSuccessListener {
                    Log.e("LOG", "ok send user")
                    //fixme
                }
    }

    fun updateStaticalInfo(airUser: AirUser?, id: String) {
        Log.d("LOG", "update statical")
        val map = HashMap<String, Any>()
        map.put("flightHours", airUser?.data?.hours?.toLong() ?: 0)
        val set = HashSet<String>()
        airUser?.data?.airports?.forEachIndexed({ index, airports ->
            set.add(airports.country)
        })
        map.put("countries", set.size)
        map.put("kilometers", airUser?.data?.kilometers?.toLong() ?: 0)
//        var airInfo = AirInfo(airUser.data.flightHours, set.size.toString(), airUser.data.kilometers)
//        hash.put("airInfo", airInfo)

        sendUserData(map, id)
    }

    fun updateFeatureTrips(body: AirUser?, uid: String) {
        val map = HashMap<String, Any>()
        val currentTime = System.currentTimeMillis()
        for (flight in body?.data?.trips?.get(0)?.flights!!) {
            Log.d("LOG", (flight.departureUtc.toLong().toString() + " " + currentTime / 1000 + " " + (flight.departureLocale.toLong() > currentTime)))
            if (flight.departureUtc.toLong() > currentTime / 1000) {
                val listCities = arrayListOf<String>()
                listCities.add(flight.origin.name)
                listCities.add(flight.destination.name)
                val listDates = arrayListOf<Long>()
                listDates.add(flight.departureUtc.toLong())
                listDates.add(flight.arrivalUtc.toLong())
                map.put("cities", listCities)
                map.put("countTrip", 1)
                map.put("dates", listDates)
                break
            }
        }
        sendUserData(map, uid)

        Log.e("LOG name", "" + body?.data?.trips?.get(0)?.flights?.get(0)?.arrivalUtc)
    }
}
