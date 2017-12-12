package ru.a1024bits.bytheway.repository

import android.arch.lifecycle.Observer
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import ru.a1024bits.bytheway.model.User
import javax.inject.Inject


const val COLLECTION_USERS = "users"

/**
 * Created by andrey.gusenkov on 19/09/2017
 */
class UserRepository @Inject constructor(val store: FirebaseFirestore) : IUsersRepository {


    var TAG = "LOG UserRepository"

    override fun getSimilarUsersTravels(data: Filter, observer: Observer<List<User>>): Task<QuerySnapshot> {
        return store.collection(COLLECTION_USERS).get()
    }

    override fun getAllUsers(): Task<QuerySnapshot> {
        return store.collection(COLLECTION_USERS).get()
    }

    override fun getReallUsers(): Task<QuerySnapshot> {
        return store.collection(COLLECTION_USERS).get() // need request for (city != null) I don't know for now how do it // fixme for performances
    }

    override fun getUserById(userID: String): Task<DocumentSnapshot> {
        return store.collection(COLLECTION_USERS).document(userID).get()
    }

    override fun addUser(user: User): Task<Void> {
        if (user.id == "1") throw FirebaseFirestoreException("User id is not set", FirebaseFirestoreException.Code.ABORTED)
        return store.collection(COLLECTION_USERS).document(user.id).set(user)
    }

    override fun changeUserProfile(map: HashMap<String, Any>, id: String): Task<Void> {
        Log.d("LOG", "change user profile send....")
        val documentRef = store.collection(COLLECTION_USERS).document(id)
        return store.runTransaction(object : Transaction.Function<Void> {
            override fun apply(transaction: Transaction): Void? {
                map.put("timestamp", FieldValue.serverTimestamp());
                documentRef.update(map)
                return null
            }
        })
    }

}