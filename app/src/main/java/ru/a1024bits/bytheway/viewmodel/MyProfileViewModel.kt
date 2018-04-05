package ru.a1024bits.bytheway.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.iid.FirebaseInstanceId
import io.reactivex.Single
import ru.a1024bits.bytheway.model.AirUser
import ru.a1024bits.bytheway.model.Response
import ru.a1024bits.bytheway.model.SocialResponse
import ru.a1024bits.bytheway.model.User
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
import ru.a1024bits.bytheway.ui.fragments.MyProfileFragment.Companion.CITY_TWO
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
import java.util.concurrent.Callable
import javax.inject.Inject


/**
 * Created by andrey.gusenkov on 25/09/2017.
 */
class MyProfileViewModel @Inject constructor(var userRepository: UserRepository) : BaseViewModel() {
    val user = MutableLiveData<User>()
    var response = MutableLiveData<Response<User>>()
    var routes = MutableLiveData<Response<RoutesList>>()
    val saveSocial = MutableLiveData<SocialResponse>()
    val saveProfile = MutableLiveData<Response<Boolean>>()
    val photoUrl = MutableLiveData<Response<String>>()
    val token = MutableLiveData<Response<Boolean>>()
    fun loadImage(pathFile: Uri, userId: String, oldUser: User?) {
        disposables.add(userRepository.uploadPhotoLink(path = pathFile, id = userId)
                .subscribeOn(getBackgroundScheduler())
                .doOnSubscribe({ _ -> loadingStatus.postValue(true) })
                .doAfterTerminate({ loadingStatus.postValue(false) })
                .flatMap({ urlPhoto -> savePhotoLink(urlPhoto, userId).subscribeOn(getBackgroundScheduler()) })
                .observeOn(getMainThreadScheduler())
                .subscribe({
                    Log.e("LOG S :", Thread.currentThread().name)
                    photoUrl.postValue(Response.success(it))
                    oldUser?.urlPhoto = it
                    user.value = oldUser
                }, { throwable ->
                    photoUrl.postValue(Response.error(throwable))
                }))
    }

    private fun savePhotoLink(downloadUrl: String, id: String): Single<String> {
        val map: HashMap<String, Any> = hashMapOf()
        map.put("urlPhoto", downloadUrl)
        return userRepository.changeUserProfile(map, id)
                .timeout(TIMEOUT_SECONDS, timeoutUnit)
                .retry(2)
                .toSingle(object : Callable<String> {
                    override fun call(): String {
                        return downloadUrl
                    }
                })
    }

    fun load(userId: String) {
        disposables.add(userRepository.getUser(userId)
                .subscribeOn(getBackgroundScheduler())
                .timeout(TIMEOUT_SECONDS, timeoutUnit)
                .retry(2)
                .doOnSubscribe({ _ -> loadingStatus.postValue(true) })
                .doOnError({ loadingStatus.postValue(false) })
                .doAfterTerminate { loadingStatus.postValue(false) }
                .observeOn(getMainThreadScheduler())
                .subscribe(
                        { user -> response.postValue(Response.success(user)) },
                        { throwable -> response.postValue(Response.error(throwable)) }
                )
        )
    }

    fun saveLinks(arraySocNetwork: HashMap<String, String>, id: String, link: SocialResponse) {
        val map: HashMap<String, Any> = hashMapOf()
        map.put("socialNetwork", arraySocNetwork)
        disposables.add(userRepository.changeUserProfile(map, id)
                .timeout(TIMEOUT_SECONDS, timeoutUnit)
                .retry(2)
                .subscribeOn(getBackgroundScheduler())
                .doOnSubscribe({ _ -> loadingStatus.postValue(true) })
                .doAfterTerminate({ loadingStatus.postValue(false) })
                .observeOn(getMainThreadScheduler())
                .subscribe(
                        { saveSocial.postValue(link) },
                        { throwable ->
                            response.postValue(Response.error(throwable))
                        }))
    }

    fun sendUserData(map: HashMap<String, Any>, id: String, oldUser: User?) {
        disposables.add(userRepository.changeUserProfile(map, id)
                .timeout(TIMEOUT_SECONDS, timeoutUnit)
                .retry(2)
                .subscribeOn(getBackgroundScheduler())
                .doOnSubscribe({ _ -> loadingStatus.postValue(true) })
                .doAfterTerminate({ loadingStatus.postValue(false) })
                .observeOn(getMainThreadScheduler())
                .subscribe({
                    saveProfile.postValue(Response.success(true))
                    user.value = makeUserFromMap(map, oldUser)
                }, { throwable ->
                    saveProfile.postValue(Response.error(throwable))
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
        if (map.containsKey(CITY_TWO)) user?.cityTwoLatLng = map[CITY_TWO] as GeoPoint
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

        if (body?.data?.trips?.isEmpty() == false && body.data.trips.get(0).flights != null) {
            for (flight in body.data.trips.get(0).flights) {
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

    fun getRoute(cityFromLatLng: GeoPoint, cityToLatLng: GeoPoint, waypoint: GeoPoint?) {
        disposables.add(userRepository.getRoute(cityFromLatLng, cityToLatLng, waypoint)
                .timeout(TIMEOUT_SECONDS, timeoutUnit)
                .retry(2)
                .subscribeOn(getBackgroundScheduler())
                .observeOn(getMainThreadScheduler())
                .subscribe({
                    routes.postValue(Response.success(it))
                }, { throwable ->
                    routes.postValue(Response.error(throwable))
                })
        )
    }

    fun updateFcmToken() {
        disposables.add(userRepository.updateFcmToken(FirebaseInstanceId.getInstance().token)
                .subscribeOn(getBackgroundScheduler())
                .observeOn(getMainThreadScheduler())
                .subscribe({
                    Log.e("LOG S :", Thread.currentThread().name)
                    token.postValue(Response.success(true))
                }, { throwable ->
                    token.postValue(Response.error(throwable))
                }))
    }
}
