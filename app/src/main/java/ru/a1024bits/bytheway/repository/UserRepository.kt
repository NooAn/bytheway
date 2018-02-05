package ru.a1024bits.bytheway.repository

import android.net.Uri
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crash.FirebaseCrash
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import io.reactivex.Completable
import io.reactivex.Single
import ru.a1024bits.bytheway.MapWebService
import ru.a1024bits.bytheway.algorithm.SearchTravelers
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.util.toJsonString
import ru.a1024bits.bytheway.viewmodel.FilterAndInstallListener
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap
import com.google.firebase.firestore.FirebaseFirestoreException


const val COLLECTION_USERS = "users"

/**
 * Created by andrey.gusenkov on 19/09/2017
 */
class UserRepository @Inject constructor(val store: FirebaseFirestore, var mapService: MapWebService) : IUsersRepository {

    companion object {
        const val TAG = "LOG UserRepository"
        const val MIN_LIMIT = 1
    }

    override fun getUser(id: String): Single<User> =
            Single.create<User> { stream ->
                try {
                    Log.e("LOG get user R", Thread.currentThread().name)
                    store.collection(COLLECTION_USERS).document(id).get().addOnSuccessListener({ document ->
                        val user = document.toObject(User::class.java)
                        stream.onSuccess(user)
                    }).addOnFailureListener({ t ->
                        stream.onError(t)
                    })
                } catch (e: Exception) {
                    stream.onError(e)
                }
            }

    override fun uploadPhotoLink(path: Uri, id: String): Single<String> = Single.create { stream ->
        try {
            Log.e("LOG uploadPhotoLink R", Thread.currentThread().name)
            // Create a storage reference from our app
            val storageRef = FirebaseStorage.getInstance().reference
            val riversRef = storageRef.child("images/" + id)
            val uploadTask = riversRef.putFile(path)
            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener {
                // Handle unsuccessful uploads
                try {
                    val errorCode = (it as StorageException).errorCode
                    val errorMessage = it.message
                    Log.e("LOG", "file fail $errorMessage and $errorCode", it)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                stream.onError(it)
            }.addOnSuccessListener { taskSnapshot ->
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                        stream.onSuccess(taskSnapshot.downloadUrl.toString())
                    }
        } catch (e: Exception) {
            e.printStackTrace()
            stream.onError(e)
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
                query = query.whereGreaterThanOrEqualTo("dates.end_date", lastTime).orderBy("dates.start_date")
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
                        .whereEqualTo("dates.end_date", 0).whereGreaterThan("cities.first_city", "").get().addOnCompleteListener({ task ->
                    listener.filterAndInstallUsers(snapshot, task.result)
                }).addOnFailureListener({ e -> listener.onFailure(e) })
            })
        } catch (e: Exception) {
            e.printStackTrace()
            FirebaseCrash.report(e)
        }
    }

    override fun getReallUsers(paramSearch: Filter): Single<List<User>> =
            Single.create<List<User>> { stream ->
                try {
                    store.collection(COLLECTION_USERS).get().addOnCompleteListener({ task ->
                        if (task.isSuccessful) {
                            Log.e("LOG get real users R", Thread.currentThread().name)

                            val result: MutableList<User> = ArrayList()
                            for (document in task.result) {
                                Log.d(TAG, document.id + " => " + document.data)

                                var user = User()
                                try {
                                    user = document.toObject(User::class.java)
                                } catch (ex2: Exception) {
                                    ex2.printStackTrace()
                                    FirebaseCrash.report(ex2)
                                }
                                try {
                                    if (user.cities.size > 0) {
                                        // run search algorithm
                                        val search = SearchTravelers(filter = paramSearch, user = user)

                                        user.percentsSimilarTravel = search.getEstimation()
                                        if (user.percentsSimilarTravel > MIN_LIMIT &&
                                                user.id != FirebaseAuth.getInstance().currentUser?.uid) {
                                            result.add(user)
                                        }
                                    }
                                } catch (ex: Exception) {
                                    stream.onError(ex)
                                    FirebaseCrash.report(ex)
                                }
                            }
                            result.sortByDescending { it.percentsSimilarTravel } // перед отправкой сортируем по степени похожести маршрута.
                            stream.onSuccess(result)
                        } else {
                            stream.onError(Exception("Not Successful load users"))
                        }
                    })
                } catch (exp: Exception) {
                    stream.onError(exp) // for fix bugs FirebaseFirestoreException: DEADLINE_EXCEEDED
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
                    Log.e("LOG change Profile R :", Thread.currentThread().name)
                    val documentRef = store.collection(COLLECTION_USERS).document(id)
                    store.runTransaction(object : Transaction.Function<Void> {
                        override fun apply(transaction: Transaction): Void? {
                            map["timestamp"] = FieldValue.serverTimestamp()
                            documentRef.update(map)
                            return null
                        }
                    }).addOnFailureListener {
                        stream.onError(it)
                    }.addOnSuccessListener { _ ->
                                stream.onComplete()
                            }
                } catch (e: Exception) {
                    stream.onError(e)
                }
            }

    override fun getRoute(cityFromLatLng: GeoPoint, cityToLatLng: GeoPoint) =
            mapService.getDirection(hashMapOf(
                    "origin" to LatLng(cityFromLatLng.latitude, cityFromLatLng.longitude).toJsonString(),
                    "destination" to LatLng(cityToLatLng.latitude, cityToLatLng.longitude).toJsonString(),
                    "sensor" to "false"))

    fun sendTime(id: String): Completable = Completable.create { stream ->
        try {
            Log.e("LOG send time R :", Thread.currentThread().name)

            val documentRef = store.collection(COLLECTION_USERS).document(id)
            store.runTransaction {
                val map = hashMapOf<String, Any>()
                map["timestamp"] = FieldValue.serverTimestamp()
                documentRef.update(map)
                null
            }.addOnFailureListener {
                        stream.onError(it)
                    }.addOnSuccessListener { _ ->
                        stream.onComplete()
                    }
        } catch (e: Exception) {
            stream.onError(e)
        }
    }
}