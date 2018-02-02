package ru.a1024bits.bytheway.ui.dialogs

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.error_registration_dialog.*
import kotlinx.android.synthetic.main.error_registration_dialog.view.*
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.ui.activity.RegistrationActivity

/**
 * Created by Олег on 02.02.2018.
 */
class ErrorStandartRegistrationDialog : DialogFragment() {
    lateinit var activity: RegistrationActivity

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.error_registration_dialog, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        view?.sendButtonCode?.setOnClickListener({
            activity.mVerificationId?.let {
                PhoneAuthProvider.getCredential(it, textFromSms.text.toString())?.let { credentialIt ->
                    activity.signInGoogle(credentialIt)
                }
            }
        })
        view?.sendButtonPhone?.setOnClickListener({
            if (!phone.text.toString().contains("+"))
                phone.setText(StringBuilder("+").append(phone.text.toString()))
            if (!activity.validatePhoneNumber(phone))
                return@setOnClickListener

            view.textFromSms.visibility = View.VISIBLE
            view.sendButtonCode.visibility = View.VISIBLE
            activity.authPhone(phone)
        })
    }

    companion object {
        fun newInstance(activity: RegistrationActivity): ErrorStandartRegistrationDialog {
            val result = ErrorStandartRegistrationDialog()
            result.activity = activity
            return result
        }
    }
}