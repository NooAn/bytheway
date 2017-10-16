package ru.a1024bits.bytheway.ui.activity

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.Toast
import ru.a1024bits.bytheway.App
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.ui.fragments.UserProfileFragment
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.android.SupportFragmentNavigator
import ru.terrakok.cicerone.commands.Command
import javax.inject.Inject


class MainActivity : FragmentActivity(), UserProfileFragment.OnFragmentInteractionListener {
    var screenNames: ArrayList<String> = arrayListOf()
    private val STATE_SCREEN_NAMES = "state_screen_names"
    
    @Inject
    lateinit var navigatorHolder: NavigatorHolder;
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component.inject(this)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar

//
//        if (savedInstanceState == null) {
//            navigator.applyCommand(Replace(Screens.USER_PROFILE_SCREEN, 1))
//        } else {
//            screenNames = savedInstanceState.getSerializable(STATE_SCREEN_NAMES) as ArrayList<String>
//        }
    
    
    }
    
    
    val navigator = object : SupportFragmentNavigator(supportFragmentManager, R.id.fragment_container) {
        override fun createFragment(screenKey: String?, data: Any?): Fragment {
            Log.e("LOG", screenKey + " " + data)
            return UserProfileFragment()
        }
        
        override fun showSystemMessage(message: String?) {
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show();
        }
        
        override fun exit() {
            finish()
        }
        
        override fun applyCommand(command: Command?) {
            super.applyCommand(command)
        }
    }
    
    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState!!.putSerializable(STATE_SCREEN_NAMES, screenNames as java.io.Serializable)
    }
    
    override fun onFragmentInteraction() {
    
    }
    
    
    override fun onResume() {
        super.onResume()
        navigatorHolder.setNavigator(navigator)
    }
    
    override fun onPause() {
        super.onPause()
        navigatorHolder.removeNavigator()
    }
    
    
}
