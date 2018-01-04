package ru.a1024bits.bytheway.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import ru.a1024bits.bytheway.model.*
import ru.a1024bits.bytheway.repository.UserRepository
import ru.a1024bits.bytheway.util.Constants.END_DATE
import ru.a1024bits.bytheway.util.Constants.FIRST_INDEX_CITY
import ru.a1024bits.bytheway.util.Constants.LAST_INDEX_CITY
import ru.a1024bits.bytheway.util.Constants.START_DATE
import javax.inject.Inject

/**
 * Created by andrey.gusenkov on 25/09/2017.
 */
class MyProfileViewModel @Inject constructor(var userRepository: UserRepository) : BaseViewModel() {
    val user: MutableLiveData<User> = MutableLiveData<User>()
    val load: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    val error: MutableLiveData<Int> = MutableLiveData<Int>()
    var response: MutableLiveData<Response<User>> = MutableLiveData()
    val loadingStatus = MutableLiveData<Boolean>()

    val saveSocial: MutableLiveData<String> = MutableLiveData()
    val clearSocial: MutableLiveData<String> = MutableLiveData()

    val saveProfile: MutableLiveData<Response<Boolean>> = MutableLiveData()

    fun load(userId: String) {
        disposables.add(userRepository.getUser(userId)
                .subscribeOn(getBackgroundScheduler())
                .observeOn(getMainThreadScheduler())
                .subscribe(
                        { user -> response.setValue(Response.success(user)) },
                        { throwable -> response.setValue(Response.error(throwable)) }
                )
        )
    }

    fun saveLinks(arraySocNetwork: HashMap<String, String>, id: String, onSuccess: () -> Unit = {}) {
        val map: HashMap<String, Any> = hashMapOf()
        map.put("socialNetwork", arraySocNetwork) // fixme
        disposables.add(userRepository.changeUserProfile(map, id)
                .subscribeOn(getBackgroundScheduler())
                .observeOn(getMainThreadScheduler())
                .doOnSubscribe({ s -> loadingStatus.setValue(true) })
                .doAfterTerminate({ loadingStatus.setValue(false) })
                .subscribe(onSuccess, { throwable ->
                    response.setValue(Response.error(throwable))
                }))
    }

    fun sendUserData(map: HashMap<String, Any>, id: String) {
        Log.e("LOG map:", id + " " + map.toString())
        disposables.add(userRepository.changeUserProfile(map, id)
                .subscribeOn(getBackgroundScheduler())
                .observeOn(getMainThreadScheduler())
                .subscribe({
                    saveProfile.setValue(Response.success(true))
                }, { throwable ->
                    response.setValue(Response.error(throwable))
                })
        )
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
        sendUserData(map, id)
    }

    fun updateFeatureTrips(body: AirUser?, uid: String) {
        val map = HashMap<String, Any>()
        val currentTime = System.currentTimeMillis()
        if (user.value?.cities?.isEmpty() == false) {
            return
        }
        if (body?.data?.trips?.isEmpty() == false && body?.data?.trips?.get(0)?.flights != null) {
            for (flight in body?.data?.trips?.get(0)?.flights) {
                Log.d("LOG", (flight.departureUtc.toLong().toString() + " " + currentTime / 1000 + " " + (flight.departureLocale.toLong() > currentTime)))
                if (flight.departureUtc.toLong() > currentTime / 1000) {
                    val mapCities = hashMapOf<String, String>()
                    mapCities.put(FIRST_INDEX_CITY, flight.origin.name)
                    mapCities.put(LAST_INDEX_CITY, flight.destination.name)
                    val mapDates = hashMapOf<String, Long>()
                    mapDates.put(START_DATE, flight.departureUtc.toLong() * 1000)
                    mapDates.put(END_DATE, flight.arrivalUtc.toLong() * 1000)
                    map.put("cities", mapCities)
                    map.put("countTrip", 1)
                    map.put("dates", mapDates)
                    break
                }
            }
            sendUserData(map, uid)
        }
    }
}
