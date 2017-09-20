package ru.a1024bits.bytheway.ui.activity

import android.arch.lifecycle.LifecycleActivity
import android.os.Bundle
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.ui.fragments.UserProfileFragment

class MainActivity : LifecycleActivity(), UserProfileFragment.OnFragmentInteractionListener {
    
    override fun onFragmentInteraction() {
    
    }
    
    companion object Factory {
        private const val TAG_FRAGMENT_PROFILE = "fragment_one"
        private const val TAG_FRAGMENT_TWO = "fragment_two"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val button = findViewById<Button>(R.id.button)
//        button.setOnClickListener {
//            //replaceFragment()
//        }
        
        if (savedInstanceState == null) {
            addFragment()
        }
        
    }
    
    private fun addFragment(allowStateLoss: Boolean = true) {
        val userProfileFragment = UserProfileFragment()
        val ft = supportFragmentManager
                .beginTransaction()
                //.setCustomAnimations(enterAnimation, exitAnimation, popEnterAnimation, popExitAnimation)
                .replace(R.id.fragment_container, userProfileFragment, TAG_FRAGMENT_PROFILE)
        
        if (!supportFragmentManager.isStateSaved) {
            ft.commit()
        } else if (allowStateLoss) {
            ft.commitAllowingStateLoss()
        }
    }
    
    
}
