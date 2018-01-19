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
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.widget.Toast


/**
 * Created by Andrei_Gusenkov on 1/19/2018.
 */

class SocNetworkdialog(context: Context, idPhone: String?) : Dialog(context) {

    override fun dismiss() {
        super.dismiss()
    }

    private val idPhone: String = idPhone ?: "Ошибка"

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = layoutInflater.inflate(R.layout.socnetwork_dialog, null)
        val text = view.findViewById<TextView>(R.id.numberPhone)
        text.text = idPhone
        text.setOnClickListener {
            val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("telegram", idPhone)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, R.string.copy_done, Toast.LENGTH_SHORT).show()
        }

        setContentView(view)
    }
}