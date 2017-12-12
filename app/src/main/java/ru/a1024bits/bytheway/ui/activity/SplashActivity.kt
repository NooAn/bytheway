package ru.a1024bits.bytheway.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_splash.*
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.util.Constants

class SplashActivity : Activity() {

    private val preferences by lazy { getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    override fun onResume() {
        super.onResume()

        val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

        val currentUser: FirebaseUser? = mAuth.currentUser

        Log.e("LOG spalsh activity", currentUser.toString())

//        Handler().postDelayed({ checkRegistrationAndForward() }, 19000L)
        Handler().postDelayed({ checkRegistrationAndForward() }, 1400L)

        var delay = 0L
        for (it in arrayOf(start_indicator_image_1, start_indicator_image_2, start_indicator_image_3)) {
            delay += 300L
            val animation = AnimationUtils.loadAnimation(this, R.anim.flash_point_with_delay)
            animation.startOffset = delay
            it.visibility = View.VISIBLE
          it.startAnimation(animation)
        }
    }

    private fun checkRegistrationAndForward() {
        //checks first enter
        if (preferences.getBoolean(Constants.FIRST_ENTER, true)) {
            startActivity(Intent(this, RegistrationActivity::class.java))
        } else {
            //if it isn't first start
            startActivity(Intent(this, MenuActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY))
        }
    }
}
