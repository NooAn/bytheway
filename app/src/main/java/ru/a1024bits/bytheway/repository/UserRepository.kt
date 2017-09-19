package ru.a1024bits.bytheway.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.a1024bits.bytheway.WebService
import ru.a1024bits.bytheway.model.User
import javax.inject.Inject


/**
 * Created by andrey.gusenkov on 19/09/2017.
 */
class UserRepository @Inject constructor(webService: WebService) {
    
    private var webservice: WebService = webService
    
    
    fun getUsers(userID: Int): LiveData<User> {
        val data = MutableLiveData<User>()
        webservice?.getUser(Integer.toString(userID))!!.enqueue(object : Callback<User> {
            override fun onFailure(call: Call<User>?, t: Throwable?) {
                Log.e("LOG", "ERROR in webservice", t)
            }
            
            override fun onResponse(call: Call<User>, response: Response<User>) {
                data.value = response.body()
            }
        })
        return data;
    }
    
    
}