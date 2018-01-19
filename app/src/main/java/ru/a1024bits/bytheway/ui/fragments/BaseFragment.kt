package ru.a1024bits.bytheway.ui.fragments

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Build
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v4.app.Fragment
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        glide = Glide.with(this)
        viewModel = ViewModelProviders.of(this, getViewFactoryClass()).get(getViewModelClass())
        viewModel?.addObserver(lifecycle)
    }

    fun showPrompt(nameScreenPrompt: String, dismissText: String?, titleText: String?, contentText: String?) {
        if (viewModel!!.promptNotShowing(nameScreenPrompt) && Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
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
        viewModel?.removeObserver(lifecycle)
        super.onDestroy()
    }

    @LayoutRes
    protected abstract fun getLayoutRes(): Int

    protected abstract fun getViewModelClass(): Class<T>
}