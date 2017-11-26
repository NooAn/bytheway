package ru.a1024bits.bytheway.ui.activity

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
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
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.a1024bits.bytheway.AirWebService
import ru.a1024bits.bytheway.App
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.model.AccessToken
import ru.a1024bits.bytheway.model.AirUser
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.router.OnFragmentInteractionListener
import ru.a1024bits.bytheway.router.Screens
import ru.a1024bits.bytheway.router.Screens.Companion.AIR_SUCCES_SCREEN
import ru.a1024bits.bytheway.router.Screens.Companion.ALL_USERS_SCREEN
import ru.a1024bits.bytheway.router.Screens.Companion.SEARCH_MAP_SCREEN
import ru.a1024bits.bytheway.router.Screens.Companion.SIMILAR_TRAVELS_SCREEN
import ru.a1024bits.bytheway.router.Screens.Companion.USER_PROFILE_SCREEN
import ru.a1024bits.bytheway.router.Screens.Companion.USER_SINHRONIZED_SCREEN
import ru.a1024bits.bytheway.ui.fragments.*
import ru.a1024bits.bytheway.util.Constants
import ru.a1024bits.bytheway.util.ServiceGenerator
import ru.a1024bits.bytheway.viewmodel.MyProfileViewModel
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.android.SupportFragmentNavigator
import ru.terrakok.cicerone.commands.Command
import ru.terrakok.cicerone.commands.Replace
import javax.inject.Inject

class MenuActivity : AppCompatActivity(),
        NavigationView.OnNavigationItemSelectedListener,
        OnFragmentInteractionListener,
        GoogleApiClient.OnConnectionFailedListener {

    private val preferences by lazy { getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE) }

    private var mGoogleApiClient: GoogleApiClient? = null

    override fun onSetPoint(l: LatLng, pos: Int) {

        val mapFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as MapFragment
        mapFragment.setMarker(l, pos)
    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }

    var screenNames: ArrayList<String> = arrayListOf()
    private val STATE_SCREEN_NAMES = "state_screen_names"

    @Inject lateinit var navigatorHolder: NavigatorHolder

    private var glide: RequestManager? = null
    var mainUser: User? = null

    private var viewModel: MyProfileViewModel? = null
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component.inject(this)
        glide = Glide.with(this)

        setContentView(R.layout.activity_menu)

        markFirstEnter()

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

        glide?.load(FirebaseAuth.getInstance().currentUser?.photoUrl)
                ?.apply(RequestOptions.circleCropTransform())
                ?.into(image)

        val fullName = hView.findViewById<TextView>(R.id.menu_fullname)
        fullName.text = FirebaseAuth.getInstance().currentUser?.displayName
        val cityName = hView.findViewById<TextView>(R.id.menu_city_name)


        // how make name and city!!?

        if (savedInstanceState == null) {
            if (true) {
                navigator.applyCommand(Replace(Screens.USER_SINHRONIZED_SCREEN, 1))
            } else
                navigator.applyCommand(Replace(Screens.USER_PROFILE_SCREEN, 1))
        } else {
            screenNames = savedInstanceState.getSerializable(STATE_SCREEN_NAMES) as ArrayList<String>
        }

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build()
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MyProfileViewModel::class.java)

    }

    private fun markFirstEnter() = preferences.edit()
            .putBoolean(Constants.FIRST_ENTER, false).apply()

    fun showUserSimpleProfile(displayingUser: User) {
        navigator.applyCommand(Replace(Screens.USER_PROFILE_SCREEN, displayingUser))
    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            drawer.openDrawer(GravityCompat.START)
        }
    }

    val navigator = object : SupportFragmentNavigator(supportFragmentManager, R.id.fragment_container) {
        override fun createFragment(screenKey: String?, data: Any?): Fragment {
            Log.e("LOG", screenKey + " " + data)
            return if (data is User)
                UserProfileFragment.newInstance(data.id)
            else
                when (screenKey) {
                    USER_PROFILE_SCREEN -> return MyProfileFragment()
                    SEARCH_MAP_SCREEN -> return MapFragment()
                    AIR_SUCCES_SCREEN -> return AirSuccesfullFragment()
                    USER_SINHRONIZED_SCREEN -> return AppInTheAirSinchronizedFragment()
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
        outState?.putSerializable(STATE_SCREEN_NAMES, screenNames as java.io.Serializable)
    }

    override fun onFragmentInteraction() {

    }


    override fun onResume() {
        super.onResume()
        navigatorHolder.setNavigator(navigator)

        // the intent filter defined in AndroidManifest will handle the return from ACTION_VIEW intent
        val uri = intent.data
        if (uri != null && uri.toString().startsWith(redirectUri)) {
            Log.e("LOGI:", uri.toString())
            // use the parameter your API exposes for the code (mostly it's "code")
            val code = uri.getQueryParameter("code")
            if (code != null) {

                Log.e("LOGI:", "code $code")
                // get access token
                // we'll do that in a minute
                val generator = ServiceGenerator()
                val loginService = generator.createService(AirWebService::class.java);
                val call = loginService.getAccessToken(code, clientId, clientSecret,
                        "authorization_code",
                        redirectUri)
                call.enqueue(object : Callback<AccessToken?> {
                    override fun onFailure(call: Call<AccessToken?>?, t: Throwable?) {
                        Log.e("LOG", "on Fail")
                    }

                    override fun onResponse(call: Call<AccessToken?>?, response: Response<AccessToken?>?) {
                        val accessToken = response?.body()
                        Log.e("LOGI", " ${accessToken?.accessToken} ${accessToken?.getTokenType()}")
                        saveToken(accessToken)
                        val loginService = generator.createService(AirWebService::class.java, accessToken?.getTokenType() + " " + accessToken?.accessToken);
                        loginService.getUserProfile().enqueue(object : Callback<AirUser?> {
                            override fun onFailure(call: Call<AirUser?>?, t: Throwable?) {
                                Log.e("LOGI", "fail", t)
                            }

                            override fun onResponse(call: Call<AirUser?>?, response: Response<AirUser?>?) {
                                Log.e("LOGI", response?.message().toString())
                                viewModel?.updateStaticalInfo(response?.body(), FirebaseAuth.getInstance().currentUser?.uid.toString())
                            }
                        })
                        loginService.getMyTrips().enqueue(object: Callback<AirUser?> {
                            override fun onResponse(call: Call<AirUser?>?, response: Response<AirUser?>?) {
                                viewModel?.updateFeatureTrips(response?.body(), FirebaseAuth.getInstance().currentUser?.uid.toString())
                            }

                            override fun onFailure(call: Call<AirUser?>?, t: Throwable?) {
                                Log.e("LOGI", "fail", t)
                            }
                        })
                        navigator.applyCommand(Replace(Screens.AIR_SUCCES_SCREEN, 1))
                    }
                })

            } else if (uri.getQueryParameter("error") != null) {
                // show an error message here
                Log.e("LOGI:", "error: ${uri.getQueryParameter("error")}")
            }
        }
    }

    private fun saveToken(accessToken: AccessToken?) {
        preferences.edit().putString(Constants.REFRESH_TOKEN, accessToken?.refresToken).apply()
        preferences.edit().putString(Constants.ACCESS_TOKEN, accessToken?.accessToken).apply()
        preferences.edit().putString(Constants.TYPE_TOKEN, accessToken?.getTokenType()).apply()
    }

    fun getAccessToken(): String = preferences.getString(Constants.ACCESS_TOKEN, "")
    fun getTypeToken(): String = preferences.getString(Constants.TYPE_TOKEN, "")
    fun getRefreshToken(): String = preferences.getString(Constants.REFRESH_TOKEN, "")


    override fun onPause() {
        super.onPause()
        navigatorHolder.removeNavigator()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
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
                preferences.edit().putBoolean(Constants.FIRST_ENTER, true).apply() //prepare for next first start
                finishAffinity()
            }
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }
}
