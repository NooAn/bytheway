package ru.a1024bits.bytheway.ui.fragments

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.util.ArrayMap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Toast
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.fragment_maps.*
import kotlinx.android.synthetic.main.fragment_search_block.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.a1024bits.aviaanimation.ui.util.LatLngInterpolator
import ru.a1024bits.aviaanimation.ui.util.MarkerAnimation
import ru.a1024bits.bytheway.App
import ru.a1024bits.bytheway.MapWebService
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.model.Status
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.model.map_directions.RoutesList
import ru.a1024bits.bytheway.repository.Filter
import ru.a1024bits.bytheway.router.Screens
import ru.a1024bits.bytheway.ui.activity.MenuActivity
import ru.a1024bits.bytheway.util.Constants
import ru.a1024bits.bytheway.util.Constants.END_DATE
import ru.a1024bits.bytheway.util.Constants.FIRST_INDEX_CITY
import ru.a1024bits.bytheway.util.Constants.LAST_INDEX_CITY
import ru.a1024bits.bytheway.util.Constants.START_DATE
import ru.a1024bits.bytheway.util.createMarker
import ru.a1024bits.bytheway.util.toJsonString
import ru.a1024bits.bytheway.viewmodel.DisplayUsersViewModel
import ru.terrakok.cicerone.commands.Forward
import uk.co.deanwild.materialshowcaseview.IShowcaseListener
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


/**
 * Created by andrey.gusenkov on 30/09/2017//
 */
class MapFragment : Fragment(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private var mMapView: MapView? = null

    private val points: ArrayMap<Int, MarkerOptions> by lazy { ArrayMap<Int, MarkerOptions>() }
    private var routeString: String? = null

    private var viewModel: DisplayUsersViewModel? = null
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    private val uid: String by lazy { FirebaseAuth.getInstance().currentUser?.uid.orEmpty() }

    @Inject lateinit var mapService: MapWebService

    var user: User = User()

    companion object {
        fun newInstance(user: User?): MapFragment {
            val fragment = MapFragment()
            fragment.arguments = Bundle()
            fragment.user = user ?: User()
            return fragment
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        App.component.inject(this)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(DisplayUsersViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_maps, container, false)

        collapsingToolbar?.setContentScrimColor(
                ContextCompat.getColor(activity, R.color.colorAccent))

        mMapView = view?.findViewById<MapView>(R.id.map)

        try {
            mMapView?.onCreate(savedInstanceState)
            MapsInitializer.initialize(activity.applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mMapView?.getMapAsync(this)

        initBoxInputFragment()

        return view
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        mMapView?.onSaveInstanceState(outState)
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onPause() {
        super.onPause()
        mMapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mMapView?.onStop()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        mMapView?.onDestroy()
        //Clean up resources from google map to prevent memory leaks.
        //Stop tracking current location
        mMap?.clear()
        mMapView = null

    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView?.onLowMemory()
    }

    private val listUsers: android.arch.lifecycle.Observer<ru.a1024bits.bytheway.model.Response<List<User>>> = android.arch.lifecycle.Observer { response ->

        when (response?.status) {
            Status.SUCCESS -> if (response.data == null) showErrorLoading() else (activity as MenuActivity).navigator.applyCommand(Forward(Screens.SIMILAR_TRAVELS_SCREEN, response.data))

            Status.ERROR -> {
                Log.e("LOG", "log e:" + response.error)
                showErrorLoading()
            }
        }
    }

    private fun showErrorLoading() {
        Log.e("LOG", "error in map rx response")
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context)
        val params = appBarLayout.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = AppBarLayout.Behavior()
        behavior.setDragCallback(object : AppBarLayout.Behavior.DragCallback() {
            override fun canDrag(appBarLayout: AppBarLayout): Boolean = false
        })
        params.behavior = behavior
        appBarLayout.layoutParams = params

        mapFragmentRoot.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                try {
                    mapFragmentRoot.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    val targetMapHeight = mapFragmentRoot.height - resources.getDimensionPixelSize(R.dimen.chooseDestinationLayoutHeight)
                    val mapParams = map.layoutParams

                    mapParams.height = targetMapHeight
                    map.layoutParams = mapParams

                    mapFragmentScrollView.scrollTo(0, 0)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })

        buttonSaveTravelInfo.setOnClickListener {
            //send data to Firebase
            viewModel?.sendUserData(getHashMapUser(), uid)
        }

        buttonSearch.setOnClickListener {
            var error = 0
            val departure = text_from_city.text.isNotEmpty()
            val destination = text_to_city.text.isNotEmpty()

            if (departure && text_from_city.text == text_to_city.text) {
                error = R.string.fill_diff_cities
            } else if (!departure && destination) {
                error = R.string.fill_from_location
            } else if (!destination && departure) {
                error = R.string.fill_to_location
            } else if (!departure && !destination) {
                error = R.string.fill_all_location
            }

            if (error != 0 && points.size < 2) {
                Toast.makeText(this@MapFragment.context, getString(error), Toast.LENGTH_SHORT).show()
            } else {
                goFlyPlan()
            }
        }

        if ((activity as MenuActivity).preferences.getBoolean("isFirstEnterMapFragment", true))
            showView = MaterialShowcaseView.Builder(activity)
                    .setTarget(buttonSaveTravelInfo)
                    .renderOverNavigationBar()
                    .setDismissText(getString(R.string.close_hint))
                    .setTitleText(getString(R.string.hint_save_and_search))
                    .setContentText(getString(R.string.hint_save_and_search_description))
                    .withCircleShape()
                    .setListener(object : IShowcaseListener {
                        override fun onShowcaseDisplayed(p0: MaterialShowcaseView?) {
                            val mHandler = Handler()
                            val time = 10000L // 10 sec after we can hide tips
                            mHandler.postDelayed({ showView?.hide() }, time)
                        }

                        override fun onShowcaseDismissed(p0: MaterialShowcaseView?) {
                            if (activity != null && !activity.isDestroyed)
                                (activity as MenuActivity).preferences.edit().putBoolean("isFirstEnterMapFragment", false).apply()
                        }
                    })
                    .show()
    }

    var showView: MaterialShowcaseView? = null

    private fun getHashMapUser(): HashMap<String, Any> {
        val hashMap = HashMap<String, Any>()
        val cities: HashMap<String, String> = hashMapOf()
        cities.put(Constants.FIRST_INDEX_CITY, searchFragment?.filter?.startCity.toString())
        cities.put(Constants.LAST_INDEX_CITY, searchFragment?.filter?.endCity.toString())
        hashMap.put("cities", cities)
        val method = searchFragment?.filter?.method
        if (method != null)
            hashMap.set("method", method)
        hashMap.put("route", routeString ?: "")
        hashMap.put("countTrip", 1)
        val dates: HashMap<String, Long> = hashMapOf()
        dates.put(START_DATE, searchFragment?.filter?.startDate ?: 0)
        dates.put(END_DATE, searchFragment?.filter?.endDate ?: 0)
        hashMap.put(MyProfileFragment.CITY_FROM,
                GeoPoint(searchFragment?.filter?.locationStartCity?.latitude ?: 0.0, searchFragment?.filter?.locationStartCity?.longitude ?: 0.0)
        )
        hashMap.put(MyProfileFragment.CITY_TO, GeoPoint(searchFragment?.filter?.locationEndCity?.latitude ?: 0.0, searchFragment?.filter?.locationEndCity?.longitude ?: 0.0))
        hashMap.put("dates", dates)
        return hashMap
    }

    var marker: Marker? = null

    override fun onMapReady(googleMap: GoogleMap) {
        this.mMap = googleMap
        var constLocation = LatLng(50.0, 50.0)

        if (points.size > 0) {
            constLocation = points.valueAt(0).position
        }
        mMap?.moveCamera(CameraUpdateFactory.newLatLng(constLocation))
        mMap?.animateCamera(CameraUpdateFactory.zoomTo(3F))
    }

    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

    fun goFlyPlan() {
        if (points.size < 2) return
        animateMarker()
        logEvents()
    }

    private fun logEvents() {
        if (searchFragment?.filter?.method?.equals(user.method) == false) {
            mFirebaseAnalytics.logEvent("Search_screen_method_change", null)
        }
        if (searchFragment?.filter?.startCity != user.cities.get(FIRST_INDEX_CITY)) {
            mFirebaseAnalytics.logEvent("Search_screen_start_city_change", null)
        }
        if (searchFragment?.filter?.endCity != user.cities.get(LAST_INDEX_CITY)) {
            mFirebaseAnalytics.logEvent("Search_screen_last_city_change", null)
        }
        if (searchFragment?.filter?.endBudget?.toLong() != user.budget.toLong()) {
            mFirebaseAnalytics.logEvent("Search_screen_end_budget_change", null)
        }
        if (searchFragment?.filter?.startDate != user.dates.get(START_DATE)) {
            mFirebaseAnalytics.logEvent("Search_screen_str_date_change", null)
        }
        if (searchFragment?.filter?.endDate != user.dates.get(END_DATE)) {
            mFirebaseAnalytics.logEvent("Search_screen_end_date_change", null)
        }
    }

    //lat = y
    //lon = x
    var searchFragment: SearchFragment? = null
    val markerAnimation = MarkerAnimation()
    var listPointPath: ArrayList<LatLng> = ArrayList()

    //Method for finding bearing between two points
    fun getBearing(begin: LatLng, end: LatLng): Float {
        val lat = Math.abs(begin.latitude - end.latitude)
        val lng = Math.abs(begin.longitude - end.longitude)

        if (begin.latitude < end.latitude && begin.longitude < end.longitude)
            return Math.toDegrees(Math.atan(lng / lat)).toFloat()
        else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)
            return (90 - Math.toDegrees(Math.atan(lng / lat)) + 90).toFloat()
        else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)
            return (Math.toDegrees(Math.atan(lng / lat)) + 180).toFloat()
        else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)
            return (90 - Math.toDegrees(Math.atan(lng / lat)) + 270).toFloat()
        return -1f
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }


    fun animateMarker() {
        // Whatever destination coordinates
        if (markerAnimation.flag == false) {

            val fromLocation = points.valueAt(0).position
            val endLocation = points.valueAt(1).position

            val markerOptions = MarkerOptions().position(fromLocation).anchor(0.5F, 1.0F).flat(true)

            var t = 0.0
            while (t < 1.000001) {
                listPointPath.add(LatLngInterpolator.CurveBezie().calculateBezierFunction(t, fromLocation, endLocation))
                t += 0.01F
            }
            drawPolyLineOnMap(listPointPath)
            // Changing marker icon
            markerOptions.icon(bitmapDescriptorFromVector(activity, R.drawable.plane)).rotation(getBearing(listPointPath.first(), listPointPath[1]))

            marker = mMap?.addMarker(markerOptions)

            markerAnimation.animateMarker(marker, listPointPath.first(), listPointPath.last(),
                    LatLngInterpolator.CurveBezie(),
                    onAnimationEnd = {
                        viewModel?.response?.observe(this@MapFragment, listUsers)
                        //  searchFragment?.filter?.endBudget = parseInt(budgetFromValue.toString())
                        // Log.d("LOG", budgetFromValue.toString())
                        viewModel?.getUsersWithSimilarTravel(searchFragment?.filter ?: Filter())
                        mMap?.clear()
                        listPointPath.clear()
                        markerAnimation.flag = false
                    })
        }
    }


    private fun initBoxInputFragment() {
        searchFragment = SearchFragment.newInstance(user)
        if (childFragmentManager != null)
            childFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_box, searchFragment, "SearchFragment")
                    .commitAllowingStateLoss()
    }

    private val PATTERN_GAP_LENGTH_PX = 20F
    private val DOT = Dot()
    private val GAP = Gap(PATTERN_GAP_LENGTH_PX)

    private val PATTERN_POLYGON_ALPHA = Arrays.asList(GAP, DOT)
    private val COLOR_BLUE_ARGB = -0x657db
    // Draw polyline on map
    fun drawPolyLineOnMap(list: List<LatLng>) {
        val strokeColor = COLOR_BLUE_ARGB
        val polyOptions = PolylineOptions()
        polyOptions.color(Color.RED)
        polyOptions.width(5f)
        polyOptions.addAll(list)
        polyOptions.color(strokeColor)
        polyOptions.pattern(PATTERN_POLYGON_ALPHA)
        mMap?.addPolyline(polyOptions)
    }

    override fun onResume() {
        super.onResume()
        try {
            mMapView?.onResume()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setMarker(point: LatLng, position: Int) {
        mMap?.clear()

        if (position == 1) {
            points.put(key = position, value = point.createMarker("Старт"))
        }
        if (position == 2) {
            points.put(key = position, value = point.createMarker("Финиш"))
        }

        //add markers on map
        points.map { it.value }.map { marker -> mMap?.addMarker(marker) }

        //animate camera to show markers
        when (points.size) {
            1 -> mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(points.valueAt(0).position, 7F/* zoom level */))
            else -> {
                mMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(createLatLngBounds(points), resources.getDimensionPixelSize(R.dimen.latLngBoundsPadding)))
                obtainDirection()
            }
        }
    }

    private fun createLatLngBounds(points: ArrayMap<Int, MarkerOptions>): LatLngBounds {
        val builder = LatLngBounds.builder()
        points.map { latLng -> builder.include(latLng.value?.position) }
        return builder.build()
    }

    fun getHashMapRoute(route: String): HashMap<String, Any> {
        val hashMap = HashMap<String, Any>()
        hashMap.put("route", route)
        return hashMap
    }

    private fun obtainDirection() {
        mapService.getDirections(hashMapOf(
                "origin" to points.valueAt(0).position.toJsonString(),
                "destination" to points.valueAt(1).position.toJsonString(),
                "sensor" to "false")).enqueue(object : Callback<RoutesList?> {
            override fun onResponse(call: Call<RoutesList?>?, response: Response<RoutesList?>?) {
                response?.body()?.routes?.map {
                    it.overviewPolyline?.encodedData?.let { routeString ->
                        this@MapFragment.routeString = routeString
                    }
                }
            }

            override fun onFailure(call: Call<RoutesList?>?, t: Throwable?) {
                t?.printStackTrace()
                //todo show error
            }
        })
    }
}