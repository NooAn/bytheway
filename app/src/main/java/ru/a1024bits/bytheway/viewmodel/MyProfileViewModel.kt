package ru.a1024bits.bytheway.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.google.firebase.firestore.GeoPoint
import ru.a1024bits.bytheway.model.*
import ru.a1024bits.bytheway.model.map_directions.RoutesList
import ru.a1024bits.bytheway.repository.UserRepository
import ru.a1024bits.bytheway.ui.fragments.MyProfileFragment
import ru.a1024bits.bytheway.ui.fragments.MyProfileFragment.Companion.ADD_INFO
import ru.a1024bits.bytheway.ui.fragments.MyProfileFragment.Companion.AGE
import ru.a1024bits.bytheway.ui.fragments.MyProfileFragment.Companion.BUDGET
import ru.a1024bits.bytheway.ui.fragments.MyProfileFragment.Companion.BUDGET_POSITION
import ru.a1024bits.bytheway.ui.fragments.MyProfileFragment.Companion.CITIES
import ru.a1024bits.bytheway.ui.fragments.MyProfileFragment.Companion.CITY
import ru.a1024bits.bytheway.ui.fragments.MyProfileFragment.Companion.CITY_FROM
import ru.a1024bits.bytheway.ui.fragments.MyProfileFragment.Companion.CITY_TO
import ru.a1024bits.bytheway.ui.fragments.MyProfileFragment.Companion.COUNT_TRIP
import ru.a1024bits.bytheway.ui.fragments.MyProfileFragment.Companion.DATES
import ru.a1024bits.bytheway.ui.fragments.MyProfileFragment.Companion.LASTNAME
import ru.a1024bits.bytheway.ui.fragments.MyProfileFragment.Companion.METHOD
import ru.a1024bits.bytheway.ui.fragments.MyProfileFragment.Companion.NAME
import ru.a1024bits.bytheway.ui.fragments.MyProfileFragment.Companion.ROUTE
import ru.a1024bits.bytheway.ui.fragments.MyProfileFragment.Companion.SEX
import ru.a1024bits.bytheway.util.Constants.END_DATE
import ru.a1024bits.bytheway.util.Constants.FIRST_INDEX_CITY
import ru.a1024bits.bytheway.util.Constants.LAST_INDEX_CITY
import ru.a1024bits.bytheway.util.Constants.START_DATE
import javax.inject.Inject

/**
 * Created by andrey.gusenkov on 25/09/2017.
 */
class MyProfileViewModel @Inject constructor(var userRepository: UserRepository) : BaseViewModel() {
    val user = MutableLiveData<User>()
    var response = MutableLiveData<Response<User>>()
    val loadingStatus = MutableLiveData<Boolean>()
    var routes = MutableLiveData<Response<RoutesList>>()
    val saveSocial = MutableLiveData<SocialResponse>()
    val saveProfile = MutableLiveData<Response<Boolean>>()

    fun load(userId: String) {
        disposables.add(userRepository.getUser(userId)
                .subscribeOn(getBackgroundScheduler())
                .observeOn(getMainThreadScheduler())
                .doOnSubscribe({ _ -> loadingStatus.setValue(true) })
                .doAfterTerminate({ loadingStatus.setValue(false) })
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
                .doOnSubscribe({ _ -> loadingStatus.setValue(true) })
                .doAfterTerminate({ loadingStatus.setValue(false) })
                .subscribe(onSuccess, { throwable ->
                    response.setValue(Response.error(throwable))
                }))
    }

    fun sendUserData(map: HashMap<String, Any>, id: String, oldUser: User?) {
        Log.e("LOG map: id = ", id + " " + map.toString())
        disposables.add(userRepository.changeUserProfile(map, id)
                .subscribeOn(getBackgroundScheduler())
                .observeOn(getMainThreadScheduler())
                .subscribe({
                    saveProfile.setValue(Response.success(true))
                    user.value = makeUserFromMap(map, oldUser)
                }, { throwable ->
                    saveProfile.setValue(Response.error(throwable))
                })
        )
    }

    private fun makeUserFromMap(map: HashMap<String, Any>, user: User?): User? {

        if (map.containsKey(CITIES)) user?.cities = map[CITIES] as HashMap<String, String>
        if (map.containsKey(DATES)) user?.dates = map[DATES] as HashMap<String, Long>
        if (map.containsKey(METHOD)) user?.method = map[METHOD] as HashMap<String, Boolean>
        if (map.containsKey(ROUTE)) user?.route = map[ROUTE] as String
        if (map.containsKey(BUDGET)) user?.budget = map[BUDGET] as Long
        if (map.containsKey(BUDGET_POSITION)) user?.budgetPosition = map[BUDGET_POSITION] as Int
        if (map.containsKey(CITY_FROM)) user?.cityFromLatLng = map[CITY_FROM] as GeoPoint
        if (map.containsKey(CITY_TO)) user?.cityToLatLng = map[CITY_TO] as GeoPoint
        if (map.containsKey(ADD_INFO)) user?.addInformation = map[ADD_INFO] as String
        if (map.containsKey(SEX)) user?.sex = map[SEX] as Int
        if (map.containsKey(COUNT_TRIP)) user?.countTrip = map[MyProfileFragment.COUNT_TRIP] as Int
        if (map.containsKey(AGE)) user?.age = map[AGE] as Int
        if (map.containsKey(NAME)) user?.name = map[NAME] as String
        if (map.containsKey(LASTNAME)) user?.lastName = map[LASTNAME] as String
        if (map.containsKey(CITY)) user?.city = map[CITY] as String

        return user
    }

    fun updateStaticalInfo(airUser: AirUser?, id: String, user: User?) {
        Log.d("LOG", "update statical")
        val map = HashMap<String, Any>()
        map.put("flightHours", airUser?.data?.hours?.toLong() ?: 0)
        val set = HashSet<String>()
        airUser?.data?.airports?.forEachIndexed({ index, airports ->
            set.add(airports.country)
        })
        map.put("countries", set.size)
        map.put("kilometers", airUser?.data?.kilometers?.toLong() ?: 0)
        sendUserData(map, id, user)
    }

    fun updateFeatureTrips(body: AirUser?, uid: String, user: User?) {
        val map = HashMap<String, Any>()
        val currentTime = System.currentTimeMillis()
//        if (user.value?.cities?.isEmpty() == false) { // I don't know why it here
//            return
//        }
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
            sendUserData(map, uid, user)
        }
    }

    fun getRoute(cityFromLatLng: GeoPoint, cityToLatLng: GeoPoint) {
        disposables.add(userRepository.getRoute(cityFromLatLng, cityToLatLng)
                .subscribeOn(getBackgroundScheduler())
                .observeOn(getMainThreadScheduler())
                .subscribe({
                    routes.setValue(Response.success(it))
                }, { throwable ->
                    routes.setValue(Response.error(throwable))
                })
        )
    }
}
