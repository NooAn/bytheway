package ru.a1024bits.bytheway.repository

import android.arch.lifecycle.Observer
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QuerySnapshot
import io.reactivex.Completable
import io.reactivex.Single
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.viewmodel.FilterAndInstallListener
import ru.a1024bits.bytheway.model.map_directions.RoutesList

interface IUsersRepository {
    fun getReallUsers(paramSearch: Filter): Single<List<User>>
    fun installAllUsers(listener: FilterAndInstallListener)
    fun getSimilarUsersTravels(data: Filter, observer: Observer<List<User>>): Task<QuerySnapshot>
    fun getUserById(userID: String): Task<DocumentSnapshot>
    fun addUser(user: User): Task<Void>

    // RX Wrapper
    fun getUser(id: String): Single<User>

    fun changeUserProfile(map: HashMap<String, Any>, id: String): Completable
    fun getRoute(cityFromLatLng: GeoPoint, cityToLatLng: GeoPoint): Single<RoutesList>
}