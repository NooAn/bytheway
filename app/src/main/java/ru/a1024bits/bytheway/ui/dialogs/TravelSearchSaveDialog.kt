package ru.a1024bits.bytheway.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.Window
import android.widget.Button
import com.google.firebase.analytics.FirebaseAnalytics
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.ui.fragments.MapFragment

/**
 * Created by Bit on 1/22/2018.
 */
class TravelSearchSaveDialog(mapFragment: MapFragment) : Dialog(mapFragment.activity) {
    val fragment = mapFragment
    protected lateinit var mFirebaseAnalytics: FirebaseAnalytics

    override fun dismiss() {
        super.dismiss()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context)
        val view = layoutInflater.inflate(R.layout.d5_save_search_dialog, null)
        view.findViewById<Button>(R.id.sendButton).setOnClickListener({ v ->
            fragment.saveData()
            mFirebaseAnalytics.logEvent("Search_screen_save", null)
            this.dismiss()
            this.cancel()
        })

        view.findViewById<Button>(R.id.cancelButton).setOnClickListener({ v ->
            mFirebaseAnalytics.logEvent("Search_screen_save_cancel", null)
            this.cancel()
        })

        setContentView(view)
    }
}