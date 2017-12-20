package ru.a1024bits.bytheway.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.f2_feedback_dialog.*
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.App


/**
 * Created by Andrei_Gusenkov on 12/20/2017.
 */
class FeedbackDialog(context: Context) : Dialog(context) {

    override fun dismiss() {
        super.dismiss()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = layoutInflater.inflate(R.layout.f2_feedback_dialog, null)
        view.findViewById<TextInputEditText>(R.id.emailText).setText(FirebaseAuth.getInstance().currentUser?.email.toString())
        view.findViewById<Button>(R.id.sendButton).setOnClickListener({ v ->
            val emailIntent = Intent(Intent.ACTION_SEND)
            emailIntent.setType("message/rfc822")
            emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf("travells2323@gmail.com"))
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Обращение от пользователя")
            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, textFeedback.text.toString())
            context.startActivity(Intent.createChooser(emailIntent, "Отправка письма. Выберите почтовый клиент"))
            this.dismiss()
            this.cancel()
        })

        view.findViewById<Button>(R.id.cancelButton).setOnClickListener({ v ->
            this.cancel()
        })

        setContentView(view)
    }
}