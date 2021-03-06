package ru.a1024bits.bytheway.repository

import android.net.Uri
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crash.FirebaseCrash
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.a1024bits.bytheway.MapWebService
import ru.a1024bits.bytheway.model.FireBaseNotification
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.model.map_directions.RoutesList
import ru.a1024bits.bytheway.util.Constants
import ru.a1024bits.bytheway.util.toJsonString
import ru.a1024bits.bytheway.viewmodel.FilterAndInstallListener
import javax.inject.Inject

const val COLLECTION_USERS = "users"

/**
 * Created by andrey.gusenkov on 19/09/2017
 */
class UserRepository @Inject constructor(val store: FirebaseFirestore, var mapService: MapWebService) : IUsersRepository {

    override fun getUser(id: String): Single<User> =
            Single.create<User> { stream ->
                try {
                    store.collection(COLLECTION_USERS).document(id).get().addOnSuccessListener({ document ->
                        if (document.exists()) {
                            val user = document.toObject(User::class.java)
                            stream.onSuccess(user)
                        }
                    }).addOnFailureListener({ t ->
                        stream.tryOnError(t)
                    })
                } catch (e: Exception) {
                    stream.tryOnError(e)
                }

            }

    override fun uploadPhotoLink(path: Uri, id: String): Single<String> = Single.create { stream ->
        try {
            val storageRef = FirebaseStorage.getInstance().reference
            val riversRef = storageRef.child("images/" + id)
            val uploadTask = riversRef.putFile(path)
            uploadTask.addOnFailureListener {
                stream.tryOnError(it)
            }.addOnSuccessListener { taskSnapshot ->
                stream.onSuccess(taskSnapshot.downloadUrl.toString())
            }
        } catch (e: Exception) {
            stream.tryOnError(e)
        }
    }


    override fun installAllUsers(listener: FilterAndInstallListener) {
        try {
            var lastTime = listener.filter.endDate
            if (listener.filter.endDate == 0L) {
                lastTime = System.currentTimeMillis()
            }
            var query = store.collection(COLLECTION_USERS).orderBy("dates.end_date")
            if (listener.filter.endDate == 0L) {
                query = query.whereGreaterThanOrEqualTo("dates.end_date", lastTime)
            } else {
                query = query.whereLessThanOrEqualTo("dates.end_date", lastTime)
            }
            query.addSnapshotListener(EventListener { snapshot, error ->
                if (error != null) {
                    listener.onFailure(error)
                    return@EventListener
                }
                if (listener.filter.endDate != 0L) {
                    listener.filterAndInstallUsers(snapshot)
                    return@EventListener
                }
                store.collection(COLLECTION_USERS)
                        .whereEqualTo("dates.end_date", 0)
                        .whereGreaterThan("cities.first_city", "")
                        .get()
                        .addOnCompleteListener({ task ->
                            listener.filterAndInstallUsers(snapshot, task.result)
                        })
                        .addOnFailureListener({ e -> listener.onFailure(e) })
            })
        } catch (e: Exception) {
            FirebaseCrash.report(e)
        }
    }

    override fun getRealUsers(): Observable<User> = Observable.create<User> { stream ->
        try {
            store.collection(COLLECTION_USERS).whereGreaterThanOrEqualTo("dates.start_date", System.currentTimeMillis())
                    .get().addOnCompleteListener({ task ->
                        if (task.isSuccessful) {
                            for (document in task.result) {
                                var user: User
                                try {
                                    user = document.toObject(User::class.java)
                                    stream.onNext(user)
                                } catch (ex2: Exception) {
                                    FirebaseCrash.report(ex2)
                                }
                            }
                        } else {
                            stream.tryOnError(Exception("Not Successful load users"))
                        }
                        stream.onComplete()
                    })
        } catch (exp: Exception) {
            stream.tryOnError(exp) // for fix bugs FirebaseFirestoreException: DEADLINE_EXCEEDED
            stream.onComplete()
        }
    }


    override fun getUserById(userID: String): Task<DocumentSnapshot> {
        return store.collection(COLLECTION_USERS).document(userID).get()
    }

    override fun addUser(user: User): Task<Void> {
        if (user.id == "1") throw FirebaseFirestoreException("User id is not set", FirebaseFirestoreException.Code.ABORTED)
        return store.collection(COLLECTION_USERS).document(user.id).set(user)
    }

    override fun changeUserProfile(map: HashMap<String, Any>, id: String): Completable =
            Completable.create { stream ->
                try {
                    val documentRef = store.collection(COLLECTION_USERS).document(id)
                    store.runTransaction(object : Transaction.Function<Void> {
                        override fun apply(transaction: Transaction): Void? {
                            map["timestamp"] = FieldValue.serverTimestamp()
                            documentRef.update(map)
                            return null
                        }
                    }).addOnFailureListener { stream.tryOnError(it) }
                            .addOnSuccessListener { _ -> stream.onComplete() }
                } catch (e: Exception) {
                    stream.tryOnError(e)
                }
            }

    override fun getRoute(cityFromLatLng: GeoPoint, cityToLatLng: GeoPoint, waypoints: GeoPoint?): Single<RoutesList> {
        val latLngPoint = if (waypoints?.latitude == 0.0 || waypoints?.longitude == 0.0 || waypoints == null) "" else LatLng(waypoints.latitude, waypoints.longitude).toJsonString()
        return mapService.getDirection(hashMapOf(
                "origin" to LatLng(cityFromLatLng.latitude, cityFromLatLng.longitude).toJsonString(),
                "destination" to LatLng(cityToLatLng.latitude, cityToLatLng.longitude).toJsonString(),
                "waypoints" to latLngPoint,
                "sensor" to "false"))
    }

    override fun sendTime(id: String): Completable = Completable.create { stream ->
        try {
            val documentRef = store.collection(COLLECTION_USERS).document(id)
            store.runTransaction {
                val map = hashMapOf<String, Any>()
                map["timestamp"] = FieldValue.serverTimestamp()
                documentRef.update(map)
                null
            }.addOnSuccessListener { _ ->
                stream.onComplete()
            }.addOnFailureListener {
                stream.tryOnError(it)
            }
        } catch (e: Exception) {
            stream.onComplete()
        }
    }

    override fun updateFcmToken(token: String?): Completable = Completable.create { stream ->
        try {
            store.runTransaction({
                val currentUid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
                if (currentUid.isNotEmpty() && token != null && token.isNotEmpty()) {

                    val docRef = FirebaseFirestore.getInstance().collection(COLLECTION_USERS)
                            .document(currentUid)
                    docRef.update(Constants.FCM_TOKEN, token)
                }
            }).addOnFailureListener {
                stream.tryOnError(it)
            }.addOnSuccessListener { stream.onComplete() }
        } catch (e: Exception) {
            stream.tryOnError(e)
        }
    }

    override fun sendNotifications(ids: String, notification: FireBaseNotification): Completable {
        return mapService.sendNotifications(hashMapOf(
                "ids" to ids,
                "title" to notification.title,
                "body" to notification.body,
                "cmd" to notification.cmd,
                "value" to notification.value.toString()
        ))
    }
}