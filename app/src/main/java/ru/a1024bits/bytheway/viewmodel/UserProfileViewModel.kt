package ru.a1024bits.bytheway.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.UserRepository
import javax.inject.Inject
import io.reactivex.schedulers.Schedulers.io
import io.reactivex.schedulers.Schedulers.single
import ru.a1024bits.bytheway.model.Response


/**
 * Created by andrey.gusenkov on 18/09/2017.
 */
class UserProfileViewModel @Inject constructor(var userRepository: UserRepository) : BaseViewModel() {

    var response: MutableLiveData<Response<User>> = MutableLiveData()

    val loadingStatus = MutableLiveData<Boolean>()

    fun load(uid: String) {
        disposables.add(userRepository.getUser(uid)
                .subscribeOn(getBackgroundScheduler())
                .observeOn(getMainThreadScheduler())
                .doOnSubscribe({ s -> loadingStatus.setValue(true) })
                .doAfterTerminate({ loadingStatus.setValue(false) })
                .subscribe(
                        { newUser -> response.setValue(Response.success(newUser)) },
                        { throwable -> response.setValue(Response.error(throwable)) }
                )
        )
    }
}