package ru.a1024bits.bytheway.ui.dialogs

import android.app.Dialog
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import ru.a1024bits.bytheway.App
import ru.a1024bits.bytheway.R
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
                // setIds.add(userId)
                viewModel.sendNotifications(userId, FireBaseNotification(
                        menuActivity.getString(R.string.app_name),
                        menuActivity.getString(R.string.this_user_search_you),
                        Constants.FCM_CMD_SHOW_USER,
                        FirebaseAuth.getInstance().currentUser?.uid
                ))
                dismiss()
                menuActivity.showSnack(menuActivity.getString(R.string.request_done))
            }
        }
        setContentView(view)
    }
}

