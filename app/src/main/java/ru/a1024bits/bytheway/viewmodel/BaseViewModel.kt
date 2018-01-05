package ru.a1024bits.bytheway.viewmodel

import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.ViewModel
import android.content.Context
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.a1024bits.bytheway.App
import ru.a1024bits.bytheway.ui.activity.MenuActivity
import ru.a1024bits.bytheway.util.Constants

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

    fun markPromptIsShowing(nameScreenPrompt: String) {
        App.INSTANCE.getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE).
                edit().putBoolean(nameScreenPrompt, false).apply()
    }

    fun promptNotShowing(nameScreenPrompt: String): Boolean =
        App.INSTANCE.getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE).getBoolean(nameScreenPrompt, true)


    override fun onCleared() {
        disposables.dispose()
        super.onCleared()
    }

}