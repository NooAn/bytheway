package ru.a1024bits.bytheway.repository

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.a1024bits.bytheway.model.FireBaseNotification
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.model.map_directions.RoutesList
import ru.a1024bits.bytheway.viewmodel.FilterAndInstallListener

interface IUsersRepository {
    fun getRealUsers(): Observable<User>
    fun installAllUsers(listener: FilterAndInstallListener)
    fun getUserById(userID: String): Task<DocumentSnapshot>
    fun addUser(user: User): Task<Void>
    fun uploadPhotoLink(path: Uri, id: String): Single<String>
    fun getUser(id: String): Single<User>
    fun changeUserProfile(map: HashMap<String, Any>, id: String): Completable
    fun getRoute(cityFromLatLng: GeoPoint, cityToLatLng: GeoPoint, waypoints: GeoPoint?): Single<RoutesList>
    fun sendNotifications(ids: String, notification: FireBaseNotification): Completable
    fun updateFcmToken(token: String?): Completable
    fun sendTime(id: String): Completable
}