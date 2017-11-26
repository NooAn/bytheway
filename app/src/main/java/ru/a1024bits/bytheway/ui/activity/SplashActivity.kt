package ru.a1024bits.bytheway.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import ru.a1024bits.bytheway.R

class SplashActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    override fun onResume() {

        super.onResume()

        val mAuth: FirebaseAuth = FirebaseAuth.getInstance();

        val currentUser: FirebaseUser? = mAuth.currentUser;

        Log.e("LOG spalsh activity", currentUser.toString())

        if (currentUser != null) {
            startActivity(Intent(this, RegistrationActivity::class.java))
        } else {
            startActivity(Intent(this, MenuActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY))
        }
    }

    override fun onStart() {
        super.onStart()
    }
}
