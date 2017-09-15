package ru.a1024bits.bytheway

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SplashActivity : Activity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }
    
    override fun onResume() {
        
        super.onResume()
        
        val mAuth: FirebaseAuth = FirebaseAuth.getInstance();
        
        val currentUser: FirebaseUser? = mAuth?.currentUser;
        
        if (currentUser == null) {
            startActivity(Intent(this, RegistrationActivity::class.java))
        } else {
            startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY))
        }
    }
    
    override fun onStart() {
        super.onStart()
    }
}
