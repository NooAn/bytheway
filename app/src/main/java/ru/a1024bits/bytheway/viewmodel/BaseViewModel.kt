package ru.a1024bits.bytheway.viewmodel

import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.ViewModel
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by Andrei_Gusenkov on 12/18/2017.
 */
open class BaseViewModel : ViewModel(), LifecycleObserver {
    val disposables = CompositeDisposable()

    fun addObserver(lifecycle: Lifecycle) {
        lifecycle.addObserver(this)
    }

    fun removeObserver(lifecycle: Lifecycle) {
        lifecycle.removeObserver(this)
    }

    protected fun getBackgroundScheduler(): Scheduler {
        return Schedulers.io()
    }

    protected fun getMainThreadScheduler(): Scheduler {
        return AndroidSchedulers.mainThread()
    }

    override fun onCleared() {
        disposables.dispose()
        super.onCleared()
    }

}