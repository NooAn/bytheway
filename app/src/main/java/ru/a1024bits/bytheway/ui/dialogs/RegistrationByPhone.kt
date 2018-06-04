package ru.a1024bits.bytheway.ui.dialogs

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.crash.FirebaseCrash
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.error_registration_dialog.*
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.ui.activity.LoginActivity

class RegistrationByPhone : DialogFragment() {
    private lateinit var activity: LoginActivity

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.error_registration_dialog, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity = super.getActivity() as LoginActivity
        isCancelable = false
        view?.let {
            sendButtonCode.setOnClickListener {
                activity.registerUserByNumber(getPhoneNumber())
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .doOnError { FirebaseCrash.report(it) }
                        .subscribe { dismissAllowingStateLoss() }
            }
            sendButtonPhone.setOnClickListener {
                if (getPhoneNumber().isNotBlank()) {
                    if (activity.validatePhoneNumber(getPhoneNumber())) {
                        verificationsData.hint = context.getString(R.string.sms_code)
                        sendButtonCode.visibility = View.VISIBLE
                        sendButtonPhone.visibility = View.GONE
                        activity.authPhone(getPhoneNumber())
                        verificationsData.setText("")
                    } else {
                        verificationsData.error = "Invalid phone number."
                    }
                }
            }
        }
    }

    private fun getPhoneNumber() = verificationsData.text.toString()
}

fun newRegistrationByPhone(): RegistrationByPhone =
        RegistrationByPhone()