package ru.a1024bits.bytheway.ui.activity

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crash.FirebaseCrash
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_menu.*
import kotlinx.android.synthetic.main.confirm_dialog.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.a1024bits.bytheway.AirWebService
import ru.a1024bits.bytheway.App
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.model.*
import ru.a1024bits.bytheway.router.OnFragmentInteractionListener
import ru.a1024bits.bytheway.router.Screens
import ru.a1024bits.bytheway.router.Screens.Companion.AIR_SUCCES_SCREEN
import ru.a1024bits.bytheway.router.Screens.Companion.ALL_USERS_SCREEN
import ru.a1024bits.bytheway.router.Screens.Companion.MY_PROFILE_SCREEN
import ru.a1024bits.bytheway.router.Screens.Companion.SEARCH_MAP_SCREEN
import ru.a1024bits.bytheway.router.Screens.Companion.SIMILAR_TRAVELS_SCREEN
import ru.a1024bits.bytheway.router.Screens.Companion.USER_SINHRONIZED_SCREEN
import ru.a1024bits.bytheway.ui.dialogs.FeedbackDialog
import ru.a1024bits.bytheway.ui.fragments.*
import ru.a1024bits.bytheway.util.Constants
import ru.a1024bits.bytheway.util.ProgressCustom
import ru.a1024bits.bytheway.util.ServiceGenerator
import ru.a1024bits.bytheway.viewmodel.MyProfileViewModel
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import ru.terrakok.cicerone.android.SupportFragmentNavigator
import ru.terrakok.cicerone.commands.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class MenuActivity : AppCompatActivity(),
        NavigationView.OnNavigationItemSelectedListener,
        OnFragmentInteractionListener,
        GoogleApiClient.OnConnectionFailedListener {

    val preferences: SharedPreferences by lazy { getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE) }

    private var mGoogleApiClient: GoogleApiClient? = null
    var pLoader: ProgressCustom? = null
    val progressBarLoad: Observer<Boolean> = Observer { b ->
        if (b == true) {
            pLoader?.show()
        } else {
            pLoader?.hide()
        }
    }

    override fun onSetPoint(l: LatLng, pos: Int, swap: Boolean) {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as MapFragment
        mapFragment.setMarker(l, pos, swap)
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.e("LOG", "${p0.errorMessage}")
    }

    var screenNames: ArrayList<String> = arrayListOf()
    private val STATE_SCREEN_NAMES = "state_screen_names"

    @Inject
    lateinit var navigatorHolder: NavigatorHolder
    @Inject
    lateinit var router: Router

    private var glide: RequestManager? = null
    var mainUser: User? = null
    var profileChanged: Boolean? = false

    private var viewModel: MyProfileViewModel? = null
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.getActiveNetworkInfo()
        return activeNetworkInfo != null && activeNetworkInfo.isConnected()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component.inject(this)
        glide = Glide.with(this)
        FirebaseCrash.setCrashCollectionEnabled(false)
        FirebaseFirestore.setLoggingEnabled(false)

        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }

        setContentView(R.layout.activity_menu)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.optionSearch, R.string.remove)
        drawer.addDrawerListener(toggle)
        toggle.syncState()


        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MyProfileViewModel::class.java)

        if (savedInstanceState == null) {
            if (preferences.getBoolean(Constants.FIRST_ENTER, true)) {
                navigator.applyCommand(Replace(Screens.USER_SINHRONIZED_SCREEN, 1))
                markFirstEnter()
            } else {
                if (intent.data != null && intent.data.host.contains("appintheair", true)) {

                } else {
                    navigator.applyCommand(Replace(Screens.MY_PROFILE_SCREEN, 1))
                }
            }
        } else {
            screenNames = savedInstanceState.getSerializable(STATE_SCREEN_NAMES) as ArrayList<String>
            Log.e("LOGGER", "hash code ${viewModel?.hashCode()}")
        }

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build()

        sing_out.setOnClickListener {
            openAwayFromProfileDialog({
                preferences.edit().putBoolean(Constants.FIRST_ENTER, true).apply()
                Log.d("tag", "FIRST_ENTER sign_out: " + preferences.getBoolean(Constants.FIRST_ENTER, true))
                FirebaseAuth.getInstance().signOut()
                finishAffinity()
            })
        }
        feedback.setOnClickListener { openDialogFeedback() }
        pLoader = this.findViewById(R.id.pLoaderRes) as ProgressCustom
        if (!isNetworkAvailable()) showSnack(getString(R.string.no_internet))
    }

    var snackbar: Snackbar? = null

    private fun showSnack(string: String) {
        snackbar = Snackbar.make(this.findViewById(android.R.id.content), string, Snackbar.LENGTH_LONG)
        snackbar?.show()
        pLoader?.hide()
    }

    private fun openProfile() {
        navigator.applyCommand(Replace(Screens.MY_PROFILE_SCREEN, 1))
        close()
    }

    private fun markFirstEnter() = preferences.edit()
            .putBoolean(Constants.FIRST_ENTER, false).apply()

    fun showUserSimpleProfile(displayingUser: User) {
        navigator.applyCommand(Forward(Screens.USER_PROFILE_SCREEN, displayingUser))
    }

    val fragmentProfile = MyProfileFragment()
    val allUsersFragment = AllUsersFragment.newInstance()

    val navigator = object : SupportFragmentNavigator(supportFragmentManager, R.id.fragment_container) {
        override fun createFragment(screenKey: String?, data: Any?): Fragment {
            return if (data is User) {
                return UserProfileFragment.newInstance(data.id)
            } else
                when (screenKey) {

                    MY_PROFILE_SCREEN -> {
                        return fragmentProfile
                    }

                    SEARCH_MAP_SCREEN -> return MapFragment.newInstance(mainUser)

                    AIR_SUCCES_SCREEN -> {
                        var name: String = ""
                        var date: String = ""
                        if (data is List<*>) {
                            name = getNameFromFligths(data as List<Fligths>)
                            date = getDateFromFligths(data as List<Fligths>)
                        }
                        return AirSuccesfullFragment.newInstance(name, date)
                    }

                    USER_SINHRONIZED_SCREEN -> return AppInTheAirSinchronizedFragment()

                    ALL_USERS_SCREEN -> return allUsersFragment

                    SIMILAR_TRAVELS_SCREEN -> {
                        SimilarTravelsFragment.newInstance(data as List<User>)
                    }
                    else -> return MapFragment()
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

    private fun getNameFromFligths(list: List<Fligths>): String {
        val currentTime = System.currentTimeMillis() / 1000
        for (flight in list) {
            if (flight.departureUtc.toLong() > currentTime) {
                return flight.origin.country + ", " + flight.origin.name + " \n" + flight.destination.country + ", " + flight.destination.name
            }
        }
        return "0"
    }

    private fun getDateFromFligths(list: List<Fligths>): String {
        val currentTime = System.currentTimeMillis() / 1000
        for (flight in list) {
            if (flight.departureUtc.toLong() > currentTime) {
                val formatter = SimpleDateFormat("dd MMM yyyy", Locale.US)
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = flight.departureUtc.toLong() * 1000
                return formatter.format(calendar.getTime())
            }
        }
        return "0"
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putSerializable(STATE_SCREEN_NAMES, screenNames as java.io.Serializable)
    }

    override fun onFragmentInteraction(user: User?) {
        mainUser = user
        updateUsersInfo(user?.urlPhoto ?: return)
    }

    private fun updateUsersInfo(url: String) {

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        val hView = navigationView.getHeaderView(0)
        hView.setOnClickListener {
            openProfile()
        }
        if (url.isNotBlank()) {
            val image = hView.findViewById<ImageView>(R.id.menu_image_avatar)
            glide?.load(url)
                    ?.apply(RequestOptions.circleCropTransform())
                    ?.into(image)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.e("LOG", "onResume")
        // the intent filter defined in AndroidManifest will handle the return from ACTION_VIEW intent
        val uri = intent.data
        if (uri != null && uri.toString().startsWith(redirectUri)) {
            Log.e("LOGI:", uri.toString())
            // use the parameter your API exposes for the code (mostly it's "code")
            val code = uri.getQueryParameter("code")
            if (code != null) {
                //FIXME перенести следующий код в viewmodel и создать репозиторий для этого.
                // get access token
                // we'll do that in a minute
                val generator = ServiceGenerator()
                val loginService = generator.createService(AirWebService::class.java)
                val call = loginService.getAccessToken(code, clientId, clientSecret,
                        "authorization_code",
                        redirectUri)
                call.enqueue(object : Callback<AccessToken?> {
                    override fun onFailure(call: Call<AccessToken?>?, t: Throwable?) {
                        Log.e("LOG", "on Fail for authorization ")
                        showSnack(getString(R.string.error_sinchronized))
                    }

                    override fun onResponse(call: Call<AccessToken?>?, response: Response<AccessToken?>?) {
                        val accessToken = response?.body()
                        saveToken(accessToken)
                        val loginService = generator.createService(AirWebService::class.java, accessToken?.getTokenType() + " " + accessToken?.accessToken)
                        loginService.getUserProfile().enqueue(object : Callback<AirUser?> {
                            override fun onFailure(call: Call<AirUser?>?, t: Throwable?) {
                                showSnack(getString(R.string.error_sinchronized))
                            }

                            override fun onResponse(call: Call<AirUser?>?, response: Response<AirUser?>?) {
                                viewModel?.updateStaticalInfo(response?.body(), FirebaseAuth.getInstance().currentUser?.uid.toString(), mainUser)
                            }
                        })
                        loginService.getMyTrips().enqueue(object : Callback<AirUser?> {
                            override fun onResponse(call: Call<AirUser?>?, response: Response<AirUser?>?) {

//                                getLatLngForAirports(response?.body()?.data?.trips?.get(0)?.flights?.get(0)?.origin?.code)
//                                getLatLngForAirports(response?.body()?.data?.trips?.get(0)?.flights?.get(0)?.destination?.code)

                                viewModel?.updateFeatureTrips(response?.body(), FirebaseAuth.getInstance().currentUser?.uid.toString(), mainUser)

                                if (response?.body() != null && response.body()?.data?.trips?.isEmpty() == false) {
                                    navigator.applyCommand(Replace(Screens.AIR_SUCCES_SCREEN, response.body()?.data?.trips?.get(0)?.flights))
                                }
                            }

                            override fun onFailure(call: Call<AirUser?>?, t: Throwable?) {
                                showSnack(getString(R.string.error_sinchronized))
                            }
                        })
                    }
                })

            } else if (uri.getQueryParameter("error") != null) {
                // show an error message here
                showSnack(getString(R.string.error_sinchronized))
            }
        }
        navigatorHolder.setNavigator(navigator)
    }

    private fun getLatLngForAirports(code: String?) {
        code?.let {
            val generator = ServiceGenerator()
            generator.createService(AirWebService::class.java)
                    .getLatLngByCode(term = code)
                    .subscribe(object : SingleObserver<Airport?> {
                        override fun onSuccess(airport: Airport) {
                            Log.e("LOG", airport.toString())
                        }

                        override fun onSubscribe(d: Disposable) {
                        }

                        override fun onError(e: Throwable) {
                        }
                    })

        }
    }

    private fun saveToken(accessToken: AccessToken?) {
        preferences.edit().putString(Constants.REFRESH_TOKEN, accessToken?.refresToken).apply()
        preferences.edit().putString(Constants.ACCESS_TOKEN, accessToken?.accessToken).apply()
        preferences.edit().putString(Constants.TYPE_TOKEN, accessToken?.getTokenType()).apply()
    }

    override fun onBackPressed() {
        navigator.applyCommand(Back())
        Log.e("LOG", "on back tap")
    }

    override fun onPause() {
        super.onPause()
        navigatorHolder.removeNavigator()
        Log.e("LOG", "onPause")

    }

    override fun onStop() {
        super.onStop()
        Log.e("LOG", "onStop")
    }

    override fun onRestart() {
        super.onRestart()
        Log.e("LOG", "onRestart")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        if (isNetworkAvailable() == false) showSnack(getString(R.string.no_internet))
        if (this.profileChanged == true) {
            openAwayFromProfileDialog({
                this.profileChanged = false
                onNavigationItemSelected(item)
            })
            return false
        } else {
            when (item.itemId) {
                R.id.profile_item -> navigator.applyCommand(Forward(Screens.MY_PROFILE_SCREEN, 1))
                R.id.search_item -> navigator.applyCommand(Forward(Screens.SEARCH_MAP_SCREEN, 1))
                R.id.all_users_item -> navigator.applyCommand(Forward(Screens.ALL_USERS_SCREEN, 1))
            }
            close()
            return true
        }
    }

    private fun close() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
    }

    private fun openAwayFromProfileDialog(callback: () -> Unit) {
        if (this.profileChanged == false) {
            callback()
            return
        }
        val simpleAlert = AlertDialog.Builder(this).create()
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.confirm_dialog, null)
        simpleAlert.setView(dialogView)
        dialogView.textMessage.text = getString(R.string.text_away_from_profile)
        simpleAlert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.yes), { dialogInterface, i ->
            val myProfile = supportFragmentManager.findFragmentById(R.id.fragment_container) as MyProfileFragment

            viewModel?.saveProfile?.observe(this, android.arch.lifecycle.Observer<ru.a1024bits.bytheway.model.Response<Boolean>> { response ->
                when (response?.status) {
                    Status.SUCCESS -> {
                        if (response.data == true) {
                            Toast.makeText(this, resources.getString(R.string.save_succesfull), Toast.LENGTH_SHORT).show()
                            callback()
                        } else {
                            showErrorOnSave()
                        }
                    }
                    Status.ERROR -> {
                        showErrorOnSave()
                        Log.e("LOG", "log e:" + response.error)
                    }
                }
            })
            viewModel?.sendUserData(myProfile.getHashMapUser(), FirebaseAuth.getInstance().currentUser?.uid.toString(), mainUser)
        })
        simpleAlert.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.no), { dialogInterface, i ->
            Log.e("LOG", " refused")
            callback()
        })
        simpleAlert.show()
    }

    private fun showErrorOnSave() {
        Toast.makeText(this, getString(R.string.error_update), Toast.LENGTH_SHORT).show()
    }

    private fun openDialogFeedback() {
        val dialog = FeedbackDialog(this)
        dialog.show()
    }
}