package ru.a1024bits.bytheway.ui.dialogs

import android.app.Dialog
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.socnetwork_dialog.*
import ru.a1024bits.bytheway.App
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.adapter.SocIconsAdapter
import ru.a1024bits.bytheway.model.FireBaseNotification
import ru.a1024bits.bytheway.model.SocialNetwork
import ru.a1024bits.bytheway.ui.activity.MenuActivity
import ru.a1024bits.bytheway.ui.fragments.MyProfileFragment.Companion.NETWORK
import ru.a1024bits.bytheway.util.Constants
import ru.a1024bits.bytheway.util.closeKeyboard
import ru.a1024bits.bytheway.util.isNumberPhone
import ru.a1024bits.bytheway.viewmodel.DisplayUsersViewModel
import javax.inject.Inject


/**
 * Created by Andrei_Gusenkov on 1/19/2018.
 */

class SocNetworkdialog(context: Context, idPhone: String?, uid: String, mFirebaseAnalytics: FirebaseAnalytics) : Dialog(context) {
    lateinit var viewModel: DisplayUsersViewModel
    val error = "Ошибка"
    private val idPhone: String = idPhone ?: error
    private val analitic = mFirebaseAnalytics
    val menuActivity: MenuActivity = context as MenuActivity
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    val userId = uid
    val ANALTAG = "soc_network_dialog"
    override fun onCreate(savedInstanceState: Bundle?) {
        App.component.inject(this)
        viewModel = ViewModelProviders.of(menuActivity, viewModelFactory).get(DisplayUsersViewModel::class.java)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = layoutInflater.inflate(R.layout.socnetwork_dialog, null)
        val text = view.findViewById<Button>(R.id.numberPhone)
        text.text = idPhone

        if (idPhone.compareTo(error) == 0) view.findViewById<TextView>(R.id.text).visibility = View.GONE
        val setIds = menuActivity.getCallNotified()
        if (setIds.contains(userId)) {
            view.findViewById<Button>(R.id.callMe).visibility = View.GONE
        }
        text.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SEND)
            emailIntent.type = "message/rfc822"
            emailIntent.data = Uri.parse("mailto:")
            emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf(text.text))
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))
            emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (emailIntent.resolveActivity(context.packageManager) == null) {
                /*
                 If we don't have email client in the phone then we just cope value from text it's all
                 */
                val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("telegram", idPhone)
                clipboard.primaryClip = clip
                analitic.logEvent("${ANALTAG}_click_copy", savedInstanceState)
                Toast.makeText(context, R.string.copy_done, Toast.LENGTH_SHORT).show()
            } else
                try {
                    if (!text.text.toString().isNumberPhone())
                        context.startActivity(Intent.createChooser(emailIntent, context.getString(R.string.email_send)))
                } catch (e: Exception) {
                    Toast.makeText(context, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
        }

        view.findViewById<Button>(R.id.callMe).setOnClickListener {
            analitic.logEvent("${ANALTAG}_click_call_me", savedInstanceState)
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
        val textForSocNetwork = view.findViewById<TextView>(R.id.textForSocNetwork)
        val rview = view.findViewById<RecyclerView>(R.id.recyclerIconsSocNetwork)
        val listApp = getSocNetworkOnPhone(menuActivity)

        rview.layoutManager = LinearLayoutManager(context, LinearLayout.HORIZONTAL, false)
        rview.hasFixedSize()
        if (menuActivity.mainUser?.socialNetwork?.size == 0 && listApp.size > 0) {
            rview.adapter = SocIconsAdapter(listApp) {
                showLinkEditText()
                when (it) {
                    R.drawable.ic_tg_color -> clickTG()
                    R.drawable.ic_whats_icon_color -> clickWh()
                    R.drawable.ic_fb_color -> clickFb()
                    R.drawable.ic_vk_color -> clickVK()
                    else -> Log.e("LOG", "click")
                }
            }
            textForSocNetwork.visibility = View.VISIBLE
        } else {
            textForSocNetwork.visibility = View.GONE
            rview.visibility = View.GONE
        }
        setContentView(view)
    }

    private fun send(s: SocialNetwork, text: String) {
        var arraySocNetwork = hashMapOf<String, String>()
        menuActivity.mainUser?.socialNetwork?.let {
            arraySocNetwork = it
        }
        val map: HashMap<String, Any> = hashMapOf()
        arraySocNetwork[s.link] = text
        map[NETWORK] = arraySocNetwork
        viewModel.sendUserData(map, FirebaseAuth.getInstance().currentUser?.uid.toString())
        menuActivity.closeKeyboard()
        link.visibility = View.GONE
        sendButton.visibility = View.GONE
        recyclerIconsSocNetwork.visibility = View.GONE
        textForSocNetwork.visibility = View.GONE
    }

    private fun initListener(text: String, net: SocialNetwork, error: String?, isCorrectValue: (s: String) -> Boolean) {
        link.setText(text)
        sendButton.setOnClickListener {
            val s = link.text.toString().replace("\\s".toRegex(), "")
            if (isCorrectValue(s))
                send(net, s)
            else link.error = error
        }
    }

    private fun clickTG() {
        analitic.logEvent("${ANALTAG}_save_dTG", null)
        var number = "+7"
        if (menuActivity.mainUser?.phone?.isNotBlank() == true && menuActivity.mainUser?.phone != "+7")
            number = menuActivity.mainUser?.phone.toString()

        initListener(number, SocialNetwork.TG, context.getString(R.string.error_link_open)) {
            it.isNumberPhone() || it.startsWith("@")
        }

    }

    private fun clickFb() {
        analitic.logEvent("${ANALTAG}_save_dFB", null)
        val fb = "https://www.facebook.com/"
        initListener(fb, SocialNetwork.FB, context.getString(R.string.error_link_open)) {
            it.startsWith(fb) && it.length > fb.length
        }
    }

    private fun clickWh() {
        analitic.logEvent("${ANALTAG}_save_dWH", null)
        var number = "+7"
        if (menuActivity.mainUser?.phone?.isNotBlank() == true) number = menuActivity.mainUser?.phone.toString()

        initListener(number, SocialNetwork.WHATSAPP, context.getString(R.string.fill_phone_invalid)) {
            it.isNumberPhone()
        }
    }

    private fun clickVK() {
        analitic.logEvent("${ANALTAG}_save_dVK", null)
        val vk = "https://www.vk.com/"
        initListener(vk, SocialNetwork.VK, context.getString(R.string.error_link_open)) {
            it.startsWith(vk) && it.length > vk.length
        }
    }

    private fun showLinkEditText() {
        link.visibility = View.VISIBLE
        sendButton.visibility = View.VISIBLE
    }

    fun getSocNetworkOnPhone(context: Context): ArrayList<Int> {
        val wh = isAppAvailable(context, "com.whatsapp")
        val fb = isAppAvailable(context, "com.facebook")
        val vk = isAppAvailable(context, "com.vkontakte.android")
        val tg = isAppAvailable(context, "org.telegram.messenger")
        // val line = isAppAvailable(context, "jp.naver.line.android") // for future
        val list = ArrayList<Int>()
        if (wh) list.add(R.drawable.ic_whats_icon_color)
        if (fb) list.add(R.drawable.ic_fb_color)
        if (vk) list.add(R.drawable.ic_vk_color)
        if (tg) list.add(R.drawable.ic_tg_color)
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

