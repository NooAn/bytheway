package ru.a1024bits.bytheway.ui.fragments

import android.support.v4.app.Fragment
import android.arch.lifecycle.*
import android.os.Bundle
import android.support.annotation.LayoutRes
import ru.a1024bits.bytheway.viewmodel.BaseViewModel
import ru.a1024bits.bytheway.viewmodel.UserProfileViewModel
import javax.inject.Inject

/**
 * Created by Andrei_Gusenkov on 12/18/2017.
 */
abstract class BaseFragment<T : BaseViewModel> : Fragment() {

    protected var viewModel: T? = null

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(getViewModelClass())
        viewModel?.addObserver(lifecycle)
    }

    override fun onDestroy() {
        viewModel?.removeObserver(lifecycle)
        super.onDestroy()
    }

    @LayoutRes
    protected abstract fun getLayoutRes(): Int

    protected abstract fun getViewModelClass(): Class<T>
}