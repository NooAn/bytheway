package ru.a1024bits.bytheway.ui.fragments

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Build
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crash.FirebaseCrash
import org.koin.android.architecture.ext.viewModel
import org.koin.android.architecture.ext.viewModelByClass
import ru.a1024bits.bytheway.viewmodel.BaseViewModel
import ru.a1024bits.bytheway.viewmodel.UserProfileViewModel
import uk.co.deanwild.materialshowcaseview.IShowcaseListener
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import kotlin.reflect.KClass

abstract class BaseFragment2<out T : BaseViewModel>(viewModelClass: KClass<T>) : Fragment() {
    protected val viewModel: T by viewModelByClass(false, viewModelClass)

    //    protected var viewModel: T? = null
    protected var glide: RequestManager? = null
    protected lateinit var mFirebaseAnalytics: FirebaseAnalytics

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        glide = Glide.with(this)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?) =
            inflater?.inflate(getLayoutRes(), container, false)

    fun showPrompt(nameScreenPrompt: String, dismissText: String?, titleText: String?, contentText: String?, button: View) {
        try {
            if (viewModel?.promptNotShowing(nameScreenPrompt) == true && Build.VERSION.SDK_INT < Build.VERSION_CODES.N && (activity != null) && !activity.isDestroyed)
                MaterialShowcaseView.Builder(activity)
                        .setTarget(button)
                        .renderOverNavigationBar()
                        .setDismissText(dismissText)
                        .setTitleText(titleText)
                        .setContentText(contentText)
                        .withCircleShape()
                        .setListener(object : IShowcaseListener {
                            override fun onShowcaseDisplayed(p0: MaterialShowcaseView?) {
                            }

                            override fun onShowcaseDismissed(p0: MaterialShowcaseView?) {
                                viewModel?.markPromptIsShowing(nameScreenPrompt)
                            }
                        })
                        .show()
        } catch (e: Exception) {
            e.printStackTrace()
            FirebaseCrash.report(e)
        }
    }

    protected abstract fun getViewFactoryClass(): ViewModelProvider.Factory

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onDestroyView() {
        viewModel?.removeObserver(lifecycle)
        super.onDestroyView()
    }

    @LayoutRes
    protected abstract fun getLayoutRes(): Int
}