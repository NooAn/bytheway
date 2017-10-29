package ru.a1024bits.bytheway.ui.activity

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import ru.a1024bits.bytheway.App
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.router.Screens
import ru.a1024bits.bytheway.ui.fragments.UserProfileFragment
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.android.SupportFragmentNavigator
import ru.terrakok.cicerone.commands.Command
import ru.terrakok.cicerone.commands.Replace
import javax.inject.Inject

class MenuActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, UserProfileFragment.OnFragmentInteractionListener {
    var screenNames: ArrayList<String> = arrayListOf()
    private val STATE_SCREEN_NAMES = "state_screen_names"
    
    @Inject
    lateinit var navigatorHolder: NavigatorHolder;
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component.inject(this)
        setContentView(R.layout.activity_menu)
        
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
    
    
        if (savedInstanceState == null) {
            navigator.applyCommand(Replace(Screens.USER_PROFILE_SCREEN, 1))
        } else {
            screenNames = savedInstanceState.getSerializable(STATE_SCREEN_NAMES) as ArrayList<String>
        }
        startActivity(Intent(this, ShowUsersActivity::class.java))
    }
    
    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
    
    val navigator = object : SupportFragmentNavigator(supportFragmentManager, R.id.fragment_container) {
        override fun createFragment(screenKey: String?, data: Any?): Fragment {
            Log.e("LOG", screenKey + " " + data)
            
            return UserProfileFragment()
        }
        
        override fun showSystemMessage(message: String?) {
            Toast.makeText(this@MenuActivity, message, Toast.LENGTH_SHORT).show();
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
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        
        return super.onOptionsItemSelected(item)
    }
    
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId
        
        if (id == R.id.profile_item) {
            navigator.applyCommand(Replace(Screens.USER_PROFILE_SCREEN, 1))
        } else if (id == R.id.search_item) {
            navigator.applyCommand(Replace(Screens.SEARCH_MAP_SCREEN, 1))
        } else if (id == R.id.exit_item) {
        
        }
        
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }
}
