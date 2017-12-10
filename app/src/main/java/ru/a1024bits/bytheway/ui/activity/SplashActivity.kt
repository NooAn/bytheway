package ru.a1024bits.bytheway.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
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

        val handler = Handler()
        handler.postDelayed({ checkRegistrationAndForward() }, 1420L)
        handler.postDelayed({
            start_indicator_image_1.startAnimation(AnimationUtils.loadAnimation(this, R.anim.flash_point_with_delay))
        }, 200)
        handler.postDelayed({
            start_indicator_image_2.startAnimation(AnimationUtils.loadAnimation(this, R.anim.flash_point_with_delay))
        }, 800)
        handler.postDelayed({
            start_indicator_image_3.startAnimation(AnimationUtils.loadAnimation(this, R.anim.flash_point_with_delay))
        }, 1400)
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
