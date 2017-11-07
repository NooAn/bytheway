package ru.a1024bits.bytheway.ui.activity

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
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.item_content_all_users.view.*
import kotlinx.android.synthetic.main.nav_header_main.*
import kotlinx.android.synthetic.main.profile_main_image.view.*
import ru.a1024bits.bytheway.App
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.router.OnFragmentInteractionListener
import ru.a1024bits.bytheway.router.Screens
import ru.a1024bits.bytheway.router.Screens.Companion.ALL_USERS_SCREEN
import ru.a1024bits.bytheway.router.Screens.Companion.SEARCH_MAP_SCREEN
import ru.a1024bits.bytheway.router.Screens.Companion.SIMILAR_TRAVELS_SCREEN
import ru.a1024bits.bytheway.router.Screens.Companion.USER_PROFILE_SCREEN
import ru.a1024bits.bytheway.ui.fragments.*
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.android.SupportFragmentNavigator
import ru.terrakok.cicerone.commands.Command
import ru.terrakok.cicerone.commands.Replace
import javax.inject.Inject

class MenuActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, OnFragmentInteractionListener {
    var screenNames: ArrayList<String> = arrayListOf()
    private val STATE_SCREEN_NAMES = "state_screen_names"

    @Inject
    lateinit var navigatorHolder: NavigatorHolder;
    private var glide: RequestManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component.inject(this)
        glide = Glide.with(this)

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
        val hView = navigationView.getHeaderView(0)
        val image = hView.findViewById<ImageView>(R.id.menu_image_avatar)

        Log.e("LOG", FirebaseAuth.getInstance().currentUser?.photoUrl.toString() + " " + image.toString())
        glide?.load(FirebaseAuth.getInstance().currentUser?.photoUrl)
                ?.into(image)
        // how make name and city!!?

        if (savedInstanceState == null) {
            navigator.applyCommand(Replace(Screens.USER_PROFILE_SCREEN, 1))
        } else {
            screenNames = savedInstanceState.getSerializable(STATE_SCREEN_NAMES) as ArrayList<String>
        }
    }

    fun showUserSimpleProfile(displayingUser: User) {
        navigator.applyCommand(Replace(Screens.USER_PROFILE_SCREEN, displayingUser))
    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            drawer.openDrawer(GravityCompat.START)
//            super.onBackPressed()
        }
    }


    val navigator = object : SupportFragmentNavigator(supportFragmentManager, R.id.fragment_container) {
        override fun createFragment(screenKey: String?, data: Any?): Fragment {
            Log.e("LOG", screenKey + " " + data)
            return if (data is User)
                UserProfileFragment.newInstance(data.name, data.lastName)
            else
                when (screenKey) {
                    USER_PROFILE_SCREEN -> return MyProfileFragment()
                    SEARCH_MAP_SCREEN -> return MapFragment()
                    ALL_USERS_SCREEN -> return AllUsersFragment.newInstance()
                    SIMILAR_TRAVELS_SCREEN -> return SimilarTravelsFragment.newInstance()
                    else -> return SearchFragment()
                }
        }

        override fun showSystemMessage(message: String?) {
            Toast.makeText(this@MenuActivity, message, Toast.LENGTH_SHORT).show()
        }

        override fun exit() {
            finish()
        }

        override fun applyCommand(command: Command?) {
            super.applyCommand(command)
            Log.e("LOG command", command.toString())
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
        // App.INSTANCE.navigatorHolder.setNavigator(navigator)
        navigatorHolder.setNavigator(navigator)
    }

    override fun onPause() {
        super.onPause()
        //  App.INSTANCE.navigatorHolder.removeNavigator()
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

        when (id) {
            R.id.profile_item -> navigator.applyCommand(Replace(Screens.USER_PROFILE_SCREEN, 1))
            R.id.search_item -> navigator.applyCommand(Replace(Screens.SEARCH_MAP_SCREEN, 1))
            R.id.all_users_item -> navigator.applyCommand(Replace(Screens.ALL_USERS_SCREEN, 1))
            R.id.similar_travel_item -> navigator.applyCommand(Replace(Screens.SIMILAR_TRAVELS_SCREEN, 1))
            R.id.exit_item -> {
            }
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }
}
