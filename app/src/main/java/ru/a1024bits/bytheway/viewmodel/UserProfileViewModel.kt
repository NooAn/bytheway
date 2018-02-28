package ru.a1024bits.bytheway.viewmodel

import android.arch.lifecycle.MutableLiveData
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.UserRepository
import javax.inject.Inject
import ru.a1024bits.bytheway.model.Response

/**
 * Created by andrey.gusenkov on 18/09/2017.
 */
class UserProfileViewModel @Inject constructor(var userRepository: UserRepository) : BaseViewModel() {

    var response: MutableLiveData<Response<User>> = MutableLiveData()

    fun load(uid: String) {
        disposables.add(userRepository.getUser(uid)
                .timeout(TIMEOUT_SECONDS, timeoutUnit)
                .retry(2)
                .subscribeOn(getBackgroundScheduler())
                .doOnSubscribe({ _ -> loadingStatus.postValue(true) })
                .doAfterTerminate({ loadingStatus.postValue(false) })
                .observeOn(getMainThreadScheduler())
                .subscribe(
                        { newUser -> response.setValue(Response.success(newUser)) },
                        { throwable -> response.setValue(Response.error(throwable)) }
                )
        )
    }
}