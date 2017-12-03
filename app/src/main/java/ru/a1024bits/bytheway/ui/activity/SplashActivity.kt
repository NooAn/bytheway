package ru.a1024bits.bytheway.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.fabric.sdk.android.Fabric
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.util.Constants

class SplashActivity : Activity() {

    private val preferences by lazy { getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Fabric.with(this, Crashlytics())
        Crashlytics.setUserEmail(FirebaseAuth.getInstance().currentUser?.email)
        Crashlytics.setUserIdentifier(FirebaseAuth.getInstance().currentUser?.uid)
    }

    override fun onResume() {

        super.onResume()

        val mAuth: FirebaseAuth = FirebaseAuth.getInstance();

        val currentUser: FirebaseUser? = mAuth.currentUser;

        Log.e("LOG spalsh activity", currentUser.toString())

        Handler().postDelayed( { checkRegistrationAndForward() }, 1000L)
    }

    private fun checkRegistrationAndForward() {
        //checks first enter
        if(preferences.getBoolean(Constants.FIRST_ENTER, true)) {
            startActivity(Intent(this, RegistrationActivity::class.java))
        } else {
            //if it isn't first start
            startActivity(Intent(this, MenuActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY))
        }
    }
}
