package ru.a1024bits.bytheway.repository

import ru.a1024bits.bytheway.MockWebService
import ru.a1024bits.bytheway.model.User
import javax.inject.Inject

class MockUserRepository @Inject constructor(private val webService: MockWebService) {
//
//    class ValAs(private val fromCount: Long, private val senderElements: ObservableEmitter<User>, private val webService: MockWebService)
//        : AsyncTask<Void, User, Void>() {
//
//        override fun doInBackground(vararg p0: Void): Void? {
//            try {
//                Thread.sleep(4000)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//            val res = webService.getChanUsers(fromCount)
//            for (i in res) {
//                publishProgress(i)
//                Log.d("tag", " " + i)
//            }
//            return null
//        }
//
//        override fun onProgressUpdate(vararg values: User) {
//            senderElements.onNext(values[0])
//        }
//
//        override fun onPostExecute(v: Void?) {
//            senderElements.onComplete()
//        }
//    }
//
//
//    fun installChanUsers(getterUsers: Observer<User>, fromCount: Long) {
//        Observable.create(ObservableOnSubscribe<User> { senderElements ->
//            //todo on retrofit
//            ValAs(fromCount, senderElements, webService).execute()
//
////            var res = webService.getChanUsers(fromCount)
////
//////                listener.onCompleteGetChanUsers(response.body()!!)
////            for (i in res) {
////                senderElements.onNext(i)
////            }
////            getterUsers.onComplete()
////                }
////            }).onResponse(res, null)
//        }).subscribe(getterUsers)
////                observable.subscribe(getterUsers)
//    }
}
