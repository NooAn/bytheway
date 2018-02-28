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

class ErrorStandartRegistrationDialog : DialogFragment() {
    lateinit var activity: RegistrationActivity

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.error_registration_dialog, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        try {
            view?.sendButtonCode?.setOnClickListener({
                activity.mVerificationId?.let {
                    PhoneAuthProvider.getCredential(it, verificationsData.text.toString())?.let { credentialIt ->
                        activity.signInGoogle(credentialIt, this@ErrorStandartRegistrationDialog)
                    }
                }
            })
            view?.sendButtonPhone?.setOnClickListener({
                if (verificationsData.text.toString().contains(Regex("^((380)|(7))")))
                    verificationsData.setText(StringBuilder("+").append(verificationsData.text.toString()))
                if (!activity.validatePhoneNumber(verificationsData))
                    return@setOnClickListener

                view.verificationsData.hint = activity.getString(R.string.sms_code)
                view.sendButtonCode.visibility = View.VISIBLE
                view.sendButtonPhone.visibility = View.GONE
                activity.authPhone(verificationsData)
                verificationsData.setText("")
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        fun newInstance(activity: RegistrationActivity): ErrorStandartRegistrationDialog {
            val result = ErrorStandartRegistrationDialog()
            result.activity = activity
            return result
        }
    }
}