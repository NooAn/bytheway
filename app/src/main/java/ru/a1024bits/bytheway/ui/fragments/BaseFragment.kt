package ru.a1024bits.bytheway.ui.fragments

import android.support.v4.app.Fragment
import android.arch.lifecycle.*
import android.os.Bundle
import android.support.annotation.LayoutRes
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import ru.a1024bits.bytheway.App
import ru.a1024bits.bytheway.ui.activity.MenuActivity
import ru.a1024bits.bytheway.viewmodel.BaseViewModel
import ru.a1024bits.bytheway.viewmodel.UserProfileViewModel
import javax.inject.Inject

/**
 * Created by Andrei_Gusenkov on 12/18/2017.
 */
abstract class BaseFragment<T : BaseViewModel> : Fragment() {

    protected var viewModel: T? = null
    protected var glide: RequestManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        glide = Glide.with(this)
        viewModel = ViewModelProviders.of(this, getViewFactoryClass()).get(getViewModelClass())
        viewModel?.addObserver(lifecycle)
    }

    protected abstract fun getViewFactoryClass(): ViewModelProvider.Factory

    override fun onDestroy() {
        viewModel?.removeObserver(lifecycle)
        super.onDestroy()
    }

    @LayoutRes
    protected abstract fun getLayoutRes(): Int

    protected abstract fun getViewModelClass(): Class<T>
}