package ru.a1024bits.bytheway.repository

import android.arch.lifecycle.Observer
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import ru.a1024bits.bytheway.model.User
import javax.inject.Inject


const val COLLECTION_USERS = "users"

/**
 * Created by andrey.gusenkov on 19/09/2017
 */
class UserRepository @Inject constructor(val store: FirebaseFirestore) : IUsersRepository {


    var TAG = "LOG UserRepository"

    init {
        Log.e("LOG", "init repos2")
    }

    override fun getSimilarUsersTravels(data: Filter, observer: Observer<List<User>>): Task<QuerySnapshot> {
        return store.collection(COLLECTION_USERS).get()
    }

    //Rx wrapper
    override fun getUsers(): Task<QuerySnapshot> {
//        val query = store.collection(COLLECTION_USERS)
//        if ((filter.startBudget != 0) && (filter.endBudget != 0)) {
//            query.whereGreaterThanOrEqualTo("budget", filter.startBudget).whereLessThanOrEqualTo("budget", filter.endBudget)
//        }
//        if ((filter.startDate != 0L) && (filter.endDate != 0L)) {
//            query.whereGreaterThanOrEqualTo("data", filter.startDate).whereLessThanOrEqualTo("data", filter.endDate)
//        }
//        if ((filter.startAge != 0) && (filter.endAge != 0)) {
//            query.whereGreaterThanOrEqualTo("age", filter.startAge).whereLessThanOrEqualTo("age", filter.endAge)
//        }
//        if (filter.sex != 0) {
//            query.whereEqualTo("sex", filter.sex)
//        }
//        if ("" != filter.startCity) {
//            query.whereEqualTo("startCity", filter.startCity)
//        }
//        if ("" != filter.endCity) {
//            query.whereEqualTo("endCity", filter.endCity)
//        }
        return store.collection(COLLECTION_USERS).get()
    }

    override fun getUserById(userID: String): Task<DocumentSnapshot> {
        return store.collection(COLLECTION_USERS).document(userID).get();
    }

    override fun addUser(user: User): Task<Void> {
        if (user.id == "1") throw FirebaseFirestoreException("User id is not set", FirebaseFirestoreException.Code.ABORTED)
        return store.collection(COLLECTION_USERS).document(user.id).set(user)
    }

    override fun changeUserProfile(map: HashMap<String, Any>, id: String): Task<Void> {
        val documentRef = store.collection(COLLECTION_USERS).document(id);
        return documentRef.update(map)
//        return store.runTransaction(object : Transaction.Function<Void> {
//            override fun apply(transaction: Transaction): Void? {
//              //  map.put("timestamp", FieldValue.serverTimestamp());
//                return null
//            }
//        })
    }

}