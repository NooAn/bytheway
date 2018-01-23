package ru.a1024bits.bytheway.repository

import android.arch.lifecycle.Observer
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import io.reactivex.Completable
import io.reactivex.Single
import ru.a1024bits.bytheway.MapWebService
import ru.a1024bits.bytheway.algorithm.SearchTravelers
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.util.toJsonString
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


const val COLLECTION_USERS = "users"

/**
 * Created by andrey.gusenkov on 19/09/2017
 */
class UserRepository @Inject constructor(val store: FirebaseFirestore, var mapService: MapWebService) : IUsersRepository {

    var TAG = "LOG UserRepository"

    companion object {
        const val TIMEOUT_SECONDS = 30L
        const val MIN_LIMIT = 1
    }

    override fun getUser(id: String): Single<User> =
            Single.create<User> { stream ->
                try {
                    store.collection(COLLECTION_USERS).document(id).get().addOnSuccessListener({ document ->
                        val user = document.toObject(User::class.java)
                        stream.onSuccess(user)
                    }).addOnFailureListener({ t ->
                        stream.onError(t)
                    })
                } catch (e: Exception) {
                    stream.onError(e)
                }
            }.timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)

    override fun getSimilarUsersTravels(data: Filter, observer: Observer<List<User>>): Task<QuerySnapshot> {
        return store.collection(COLLECTION_USERS).get()
    }

    override fun getAllUsers(): Single<MutableList<User>> {
        Log.e("LOG get all users", Thread.currentThread().name)
        return Single.create<MutableList<User>> { stream ->
            try {
                store.collection(COLLECTION_USERS)
                        .get()
                        .addOnCompleteListener({ task ->
                            Log.e("LOG completeListener", Thread.currentThread().name)
                            val result: MutableList<User> = arrayListOf()
                            for (document in task.result) {
                                try {
                                    val user = document.toObject(User::class.java)
                                    if (user.cities.size > 0 && user.id != FirebaseAuth.getInstance().currentUser?.uid) {
                                        result.add(user)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            stream.onSuccess(result)
                        }).addOnFailureListener({ exception -> stream.onError(exception) })
            } catch (exp: Exception) {
                stream.onError(exp) // for fix bugs FirebaseFirestoreException: DEADLINE_EXCEEDED
            }
        }.timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
    }

    override fun getReallUsers(paramSearch: Filter): Single<List<User>> =
            Single.create<List<User>> { stream ->
                try {
                    store.collection(COLLECTION_USERS).get().addOnCompleteListener({ task ->
                        if (task.isSuccessful) {
                            Log.e("LOG get all users", Thread.currentThread().name)

                            val result: MutableList<User> = ArrayList()
                            for (document in task.result) {
                                Log.d(TAG, document.id + " => " + document.data)

                                var user = User()
                                try {
                                    user = document.toObject(User::class.java)
                                } catch (ex2: Exception) {
                                    Log.e(TAG, "Error!: " + document.id + " => " + document.data, ex2)
                                    ex2.printStackTrace()
                                }
                                try {
                                    if (user.cities.size > 0) {
                                        // run search algorithm
                                        val search = SearchTravelers(filter = paramSearch, user = user)
                                        val s = search.getEstimation()
                                        user.percentsSimilarTravel = if (s > 100) 100 else s
                                        if (user.percentsSimilarTravel > MIN_LIMIT &&
                                                user.id != FirebaseAuth.getInstance().currentUser?.uid) {
                                            result.add(user)
                                        }
                                    }
                                } catch (ex: Exception) {
                                    stream.onError(ex)
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

            }.timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)

    override fun getUserById(userID: String): Task<DocumentSnapshot> {
        return store.collection(COLLECTION_USERS).document(userID).get()
    }

    override fun addUser(user: User): Task<Void> {
        if (user.id == "1") throw FirebaseFirestoreException("User id is not set", FirebaseFirestoreException.Code.ABORTED)
        return store.collection(COLLECTION_USERS).document(user.id).set(user)
    }

    override fun changeUserProfile(map: HashMap<String, Any>, id: String): Completable =
            Completable.create { stream ->
                Log.d("LOG", "change user profile send....")
                try {
                    val documentRef = store.collection(COLLECTION_USERS).document(id)
                    store.runTransaction(object : Transaction.Function<Void> {
                        override fun apply(transaction: Transaction): Void? {
                            map.put("timestamp", FieldValue.serverTimestamp())
                            documentRef.update(map)
                            return null
                        }
                    }).addOnCompleteListener {
                        Log.e("LOG", "finish update user profile")
                    }.addOnFailureListener {
                                stream.onError(it)
                            }.addOnSuccessListener { _ ->
                                stream.onComplete()
                            }
                } catch (e: Exception) {
                    stream.onError(e)
                }
            }.timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)

    override fun getRoute(cityFromLatLng: GeoPoint, cityToLatLng: GeoPoint) =
            mapService.getDirection(hashMapOf(
                    "origin" to LatLng(cityFromLatLng.latitude, cityFromLatLng.longitude).toJsonString(),
                    "destination" to LatLng(cityToLatLng.latitude, cityToLatLng.longitude).toJsonString(),
                    "sensor" to "false"))
}