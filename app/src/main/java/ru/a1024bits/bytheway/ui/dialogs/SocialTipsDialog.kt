package ru.a1024bits.bytheway.ui.dialogs

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import ru.a1024bits.bytheway.R


/**
 * Created by Andrei_Gusenkov on 1/22/2018.
 */
class SocialTipsDialog : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater.inflate(R.layout.fragment_social_tips_dialog, container, false)
        getDialog().setTitle(getString(R.string.attention))
        rootView.findViewById<Button>(R.id.dismiss).setOnClickListener{
            dismiss()
        }
        return rootView
    }
}