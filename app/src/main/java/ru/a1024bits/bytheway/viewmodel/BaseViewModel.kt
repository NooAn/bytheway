package ru.a1024bits.bytheway.viewmodel

import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.ViewModel

/**
 * Created by Andrei_Gusenkov on 12/18/2017.
 */
open class BaseViewModel : ViewModel(), LifecycleObserver{
 //   private val disposables = CompositeDisposable()


    fun addObserver(lifecycle: Lifecycle) {
        lifecycle.addObserver(this)
    }

    fun removeObserver(lifecycle: Lifecycle) {
        lifecycle.removeObserver(this)
    }

    override fun onCleared() {
       // disposables.dispose() for RX JAVA
        super.onCleared()
    }

}