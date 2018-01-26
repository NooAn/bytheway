package ru.a1024bits.bytheway.ui.fragments

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Build
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.annotation.NonNull
import android.support.annotation.Nullable
import android.support.design.R.id.container
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.fragment_display_all_users.*
import ru.a1024bits.bytheway.viewmodel.BaseViewModel
import uk.co.deanwild.materialshowcaseview.IShowcaseListener
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView

/**
 * Created by Andrei_Gusenkov on 12/18/2017.
 */
abstract class BaseFragment<T : BaseViewModel> : Fragment() {

    protected var viewModel: T? = null
    protected var glide: RequestManager? = null
    protected lateinit var mFirebaseAnalytics: FirebaseAnalytics

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        glide = Glide.with(this)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context)
        viewModel = ViewModelProviders.of(this, getViewFactoryClass()).get(getViewModelClass())
        viewModel?.addObserver(lifecycle)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(getLayoutRes(), container, false)
        return view
    }

    fun showPrompt(nameScreenPrompt: String, dismissText: String?, titleText: String?, contentText: String?) {
        if (viewModel?.promptNotShowing(nameScreenPrompt) == true && Build.VERSION.SDK_INT < Build.VERSION_CODES.N  && (activity != null) && !activity.isDestroyed && searchParametersText != null)
            MaterialShowcaseView.Builder(activity)
                    .setTarget(searchParametersText)
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
    }

    protected abstract fun getViewFactoryClass(): ViewModelProvider.Factory

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel?.removeObserver(lifecycle)
    }

    @LayoutRes
    protected abstract fun getLayoutRes(): Int

    protected abstract fun getViewModelClass(): Class<T>
}