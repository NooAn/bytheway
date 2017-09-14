package ru.a1024bits.bytheway

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class SplashActivity : Activity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        startActivity(Intent(this, RegistrationActivity::class.java))
    }
}
