package ru.a1024bits.bytheway.ui.dialogs

import android.app.Dialog
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import ru.a1024bits.bytheway.App
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.adapter.SimilarTravelsAdapter
import ru.a1024bits.bytheway.adapter.SocIconsAdapter
import ru.a1024bits.bytheway.model.FireBaseNotification
import ru.a1024bits.bytheway.ui.activity.MenuActivity
import ru.a1024bits.bytheway.util.Constants
import ru.a1024bits.bytheway.util.joinToString
import ru.a1024bits.bytheway.viewmodel.DisplayUsersViewModel
import javax.inject.Inject


/**
 * Created by Andrei_Gusenkov on 1/19/2018.
 */

class SocNetworkdialog(context: Context, idPhone: String?, uid: String, mFirebaseAnalytics: FirebaseAnalytics) : Dialog(context) {
    lateinit var viewModel: DisplayUsersViewModel
    private val idPhone: String = idPhone ?: "Ошибка"
    private val analitic = mFirebaseAnalytics
    val menuActivity: MenuActivity = context as MenuActivity
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    val userId = uid

    override fun onCreate(savedInstanceState: Bundle?) {
        App.component.inject(this)
        viewModel = ViewModelProviders.of(menuActivity, viewModelFactory).get(DisplayUsersViewModel::class.java)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = layoutInflater.inflate(R.layout.socnetwork_dialog, null)
        val text = view.findViewById<Button>(R.id.numberPhone)
        text.text = idPhone
        if (idPhone.compareTo("Ошибка") == 0) view.findViewById<TextView>(R.id.text).visibility = View.GONE

        text.setOnClickListener {
            val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("telegram", idPhone)
            clipboard.primaryClip = clip
            analitic.logEvent("soc_network_dialog_click_copy", savedInstanceState)
            Toast.makeText(context, R.string.copy_done, Toast.LENGTH_SHORT).show()
        }

        view.findViewById<Button>(R.id.callMe).setOnClickListener {
            analitic.logEvent("soc_network_dialog_click_call_me", savedInstanceState)
            val setIds = menuActivity.getCallNotified()
            if (!setIds.contains(userId)) {
                setIds.add(userId)
                viewModel.sendNotifications(userId, FireBaseNotification(
                        menuActivity.getString(R.string.app_name),
                        menuActivity.getString(R.string.this_user_search_you),
                        Constants.FCM_CMD_SHOW_USER,
                        FirebaseAuth.getInstance().currentUser?.uid
                ))
                menuActivity.updateSetCallNotify(setIds)
                dismiss()
                menuActivity.showSnack(menuActivity.getString(R.string.request_done))
            }
        }
        val rview = view.findViewById<RecyclerView>(R.id.recyclerIconsSocNetwork)
        val listApp = getSocNetworkOnPhone(menuActivity)
        if (listApp.size > 0)
            rview.adapter = SocIconsAdapter(listApp) {
                when (it) {
                    R.drawable.ic_tg_color -> clickTG()
                    R.drawable.ic_whats_icon_color -> clickWh()
                    R.drawable.ic_fb_color -> clickFb()
                    R.drawable.ic_vk_color -> clickVK()
                    else -> Log.e("LOG", "click")
                }
            }
        else
            rview.visibility = View.GONE
        setContentView(view)
    }

    private fun clickTG() {
        Log.e("LOG", "click1")
    }

    private fun clickFb() {

    }

    private fun clickWh() {

    }

    private fun clickVK() {

    }

    fun getSocNetworkOnPhone(context: Context): ArrayList<Int> {
        val wh = isAppAvailable(context, "com.whatsapp")
        val fb = isAppAvailable(context, "com.facebook")
        val vk = isAppAvailable(context, "com.vkontakte.android")
        val tg = isAppAvailable(context, "org.telegram.messenger")
        // val line = isAppAvailable(context, "jp.naver.line.android") // for future
        val list = ArrayList<Int>()
        if (!wh) list.add(R.drawable.ic_whats_icon_color)
        if (!fb) list.add(R.drawable.ic_fb_color)
        if (!vk) list.add(R.drawable.ic_vk_color)
        if (!tg) list.add(R.drawable.ic_tg_color)
        return list
    }

    /**
    +     * Indicates whether the specified app ins installed and can used as an intent. This
    +     * method checks the package manager for installed packages that can
    +     * respond to an intent with the specified app. If no suitable package is
    +     * found, this method returns false.
    +     *
    +     * @param context The application's environment.
    +     * @param appName The name of the package you want to check
    +     *
    +     * @return True if app is installed
    +     */
    fun isAppAvailable(context: Context, appName: String): Boolean {
        try {
            context.packageManager.getPackageInfo(appName, PackageManager.GET_ACTIVITIES)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }
    }
}

