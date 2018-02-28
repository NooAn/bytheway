package ru.a1024bits.bytheway.ui.fragments

import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
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
import com.google.firebase.crash.FirebaseCrash
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.fragment_maps.*
import kotlinx.android.synthetic.main.fragment_search_block.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.a1024bits.aviaanimation.ui.util.LatLngInterpolator
import ru.a1024bits.aviaanimation.ui.util.MarkerAnimation
import ru.a1024bits.bytheway.App
import ru.a1024bits.bytheway.BuildConfig
import ru.a1024bits.bytheway.MapWebService
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.model.FireBaseNotification
import ru.a1024bits.bytheway.model.Method
import ru.a1024bits.bytheway.model.Status
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.model.map_directions.RoutesList
import ru.a1024bits.bytheway.repository.Filter
import ru.a1024bits.bytheway.router.Screens
import ru.a1024bits.bytheway.ui.activity.MenuActivity
import ru.a1024bits.bytheway.ui.dialogs.TravelSearchSaveDialog
import ru.a1024bits.bytheway.ui.fragments.MyProfileFragment.Companion.BUDGET
import ru.a1024bits.bytheway.util.Constants
import ru.a1024bits.bytheway.util.Constants.END_DATE
import ru.a1024bits.bytheway.util.Constants.FCM_CMD_SHOW_USER
import ru.a1024bits.bytheway.util.Constants.FIRST_INDEX_CITY
import ru.a1024bits.bytheway.util.Constants.LAST_INDEX_CITY
import ru.a1024bits.bytheway.util.Constants.START_DATE
import ru.a1024bits.bytheway.util.createMarker
import ru.a1024bits.bytheway.util.toJsonString
import ru.a1024bits.bytheway.viewmodel.DisplayUsersViewModel
import ru.terrakok.cicerone.commands.Forward
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


/**
 * Created by andrey.gusenkov on 30/09/2017//
 */
class MapFragment : BaseFragment<DisplayUsersViewModel>(), OnMapReadyCallback {

    override fun getViewFactoryClass(): ViewModelProvider.Factory = viewModelFactory


    @LayoutRes
    override fun getLayoutRes(): Int {
        return R.layout.fragment_maps
    }

    override fun getViewModelClass(): Class<DisplayUsersViewModel> = DisplayUsersViewModel::class.java


    private var mMap: GoogleMap? = null
    private var mMapView: MapView? = null

    private val points: ArrayMap<Int, MarkerOptions> by lazy { ArrayMap<Int, MarkerOptions>() }
    private var routeString: String? = null

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val uid: String by lazy { FirebaseAuth.getInstance().currentUser?.uid.orEmpty() }

    @Inject
    lateinit var mapService: MapWebService

    var user: User = User()

    companion object {
        const val DATES = "dates"
        const val CITIES = "cities"
        const val METHOD = "method"
        const val COUNT_TRIP = "countTrip"
        const val ROUTE = "route"
        fun newInstance(user: User?): MapFragment {
            val fragment = MapFragment()
            fragment.arguments = Bundle()
            fragment.user = user ?: User()
            return fragment
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        collapsingToolbar?.setContentScrimColor(
                ContextCompat.getColor(activity, R.color.colorAccent))

        mMapView = view?.findViewById(R.id.map)

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
        try {
            mMapView?.onSaveInstanceState(outState)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
//        mMapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        try {
            mMapView?.onStop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        try {
            mMapView?.onDestroy()
            //Clean up resources from google map to prevent memory leaks.
            //Stop tracking current location
            mMap?.clear()
            mMapView = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView?.onLowMemory()
    }

    private val listUsers: android.arch.lifecycle.Observer<ru.a1024bits.bytheway.model.Response<List<User>>> = android.arch.lifecycle.Observer { response ->

        when (response?.status) {
            Status.SUCCESS -> {
                if (response.data == null && activity != null) {
                    showErrorLoading()
                } else {

                    val notifyIdsForUsers = arrayListOf<String>()
                    val saveNotifiedIds = (activity as MenuActivity).getNotified()
                    response.data?.map {
                        if (it.percentsSimilarTravel >= Constants.FCM_MATCH_PERCENT && !saveNotifiedIds.contains(it.id)) {
                            notifyIdsForUsers.add(it.id)
                            saveNotifiedIds.add(it.id)
                        }
                    }
                    if (notifyIdsForUsers.size > 0) {
                        if (BuildConfig.DEBUG) notifyIdsForUsers.add(FirebaseAuth.getInstance().currentUser?.uid!!)
                        viewModel?.sendNotifications(notifyIdsForUsers.joinToString(","), FireBaseNotification(
                                getString(R.string.app_name),
                                getString(R.string.traveller) + "  ${user.name}" + getString(R.string.notification_user_searching),
                                FCM_CMD_SHOW_USER,
                                FirebaseAuth.getInstance().currentUser?.uid
                        ))
                        //(activity as MenuActivity).updateNotified(notifiedIds)
                    }
                }

                (activity as MenuActivity).navigator.applyCommand(Forward(Screens.SIMILAR_TRAVELS_SCREEN, response.data))
            }


            Status.ERROR -> {
                Log.e("LOG", "log e:" + response.error)
                showErrorLoading()
            }
        }
    }

    private fun showErrorLoading() {
        Toast.makeText(activity, R.string.just_error, Toast.LENGTH_SHORT).show()
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
            openDialogSave()
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
        showPrompt("isFirstEnterMapFragment", context.resources.getString(R.string.close_hint),
                getString(R.string.hint_save_and_search), getString(R.string.hint_save_and_search_description), buttonSaveTravelInfo)

    }

    private fun openDialogSave() {
        val dialog = TravelSearchSaveDialog(this)
        dialog.show()
    }

    fun saveData() {
        //send data to Firebase
        try {
            viewModel?.sendUserData(getHashMapUser(), uid)
            changeUserData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun changeUserData() {
        user.cities = getCitiesMap()
        user.dates = getDatesMap()
        user.budget = getCurrentBudget()
    }

    private fun getHashMapUser(): HashMap<String, Any> {
        val hashMap = HashMap<String, Any>()

        hashMap[CITIES] = getCitiesMap()

        searchFragment?.filter?.method?.let {
            hashMap[METHOD] = it
        }

        hashMap[ROUTE] = routeString ?: ""
        hashMap[COUNT_TRIP] = 1
        hashMap[BUDGET] = getCurrentBudget()

        hashMap[MyProfileFragment.CITY_FROM] = GeoPoint(searchFragment?.filter?.locationStartCity?.latitude
                ?: 0.0, searchFragment?.filter?.locationStartCity?.longitude ?: 0.0)

        hashMap[MyProfileFragment.CITY_TO] = GeoPoint(searchFragment?.filter?.locationEndCity?.latitude
                ?: 0.0, searchFragment?.filter?.locationEndCity?.longitude ?: 0.0)

        hashMap[DATES] = getDatesMap()
        return hashMap
    }

    private fun getCurrentBudget() = searchFragment?.filter?.endBudget?.toLong() ?: user.budget

    private fun getDatesMap(): HashMap<String, Long> = hashMapOf(
            START_DATE to (searchFragment?.filter?.startDate ?: 0L),
            END_DATE to (searchFragment?.filter?.endDate ?: 0L))

    private fun getCitiesMap(): HashMap<String, String> = hashMapOf(
            FIRST_INDEX_CITY to searchFragment?.filter?.startCity.toString(),
            LAST_INDEX_CITY to searchFragment?.filter?.endCity.toString())

    private var marker: Marker? = null

    override fun onMapReady(googleMap: GoogleMap) {
        this.mMap = googleMap
        var constLocation = LatLng(50.0, 50.0)
        try {
            if (points.size > 0) {
                setMarker(points.valueAt(0).position, 1)
                setMarker(points.valueAt(1).position, 2)
            } else {
                mMap?.moveCamera(CameraUpdateFactory.newLatLng(constLocation))
                mMap?.animateCamera(CameraUpdateFactory.zoomTo(3F))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            FirebaseCrash.report(e)
        }
    }

    private fun goFlyPlan() {
        if (points.size < 2) return
        animateMarker()
        logEvents()
    }

    private fun logEvents() {
        mFirebaseAnalytics.logEvent("Search_screen_search", null)

        if (searchFragment?.filter?.method?.equals(user.method) == false) {
            mFirebaseAnalytics.logEvent("Search_screen_method_change", null)
        }
        if (searchFragment?.filter?.startCity != user.cities[FIRST_INDEX_CITY]) {
            mFirebaseAnalytics.logEvent("Search_screen_start_city_change", null)
        }
        if (searchFragment?.filter?.endCity != user.cities[LAST_INDEX_CITY]) {
            mFirebaseAnalytics.logEvent("Search_screen_last_city_change", null)
        }
        if (searchFragment?.filter?.endBudget?.toLong() != user.budget) {
            mFirebaseAnalytics.logEvent("Search_screen_end_budget_change", null)
        }
        if (searchFragment?.filter?.startDate != user.dates[START_DATE]) {
            mFirebaseAnalytics.logEvent("Search_screen_str_date_change", null)
        }
        if (searchFragment?.filter?.endDate != user.dates[END_DATE]) {
            mFirebaseAnalytics.logEvent("Search_screen_end_date_change", null)
        }
    }

    //lat = y
    //lon = x
    private
    var searchFragment: SearchFragment? = null
    private
    val markerAnimation = MarkerAnimation()
    private
    var listPointPath: ArrayList<LatLng> = ArrayList()

    //Method for finding bearing between two points
    private fun getBearing(begin: LatLng, end: LatLng): Float {
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


    private fun animateMarker() {
        // Whatever destination coordinates
        if (!markerAnimation.flag) {

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
                        viewModel?.getUsersWithSimilarTravel(searchFragment?.filter
                                ?: Filter())
                        //  mMap?.clear()
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

    private
    val PATTERN_GAP_LENGTH_PX = 20F
    private
    val DOT = Dot()
    private
    val GAP = Gap(PATTERN_GAP_LENGTH_PX)

    private
    val PATTERN_POLYGON_ALPHA = Arrays.asList(GAP, DOT)
    private
    val COLOR_BLUE_ARGB = -0x657db

    // Draw polyline on map
    private fun drawPolyLineOnMap(list: List<LatLng>) {
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

    fun setMarker(point: LatLng, position: Int, swap: Boolean = false) {
        mMap?.clear()

        if (position == 1) {
            points.put(key = position, value = point.createMarker(getString(R.string.start)))
        }
        if (position == 2) {
            points.put(key = position, value = point.createMarker(getString(R.string.finish)))
        }

        //add markers on map
        points.map { it.value }.map { marker -> mMap?.addMarker(marker) }
        //animate camera to show markers
        try {
            when (points.size) {
                1 -> mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(points.valueAt(0).position, 7F/* zoom level */))
                else -> {
                    mMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(createLatLngBounds(points), resources.getDimensionPixelSize(R.dimen.latLngBoundsPadding)))
                    if (!swap)
                        obtainDirection()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            FirebaseCrash.report(e)
        }
    }

    private fun createLatLngBounds(points: ArrayMap<Int, MarkerOptions>): LatLngBounds {
        val builder = LatLngBounds.builder()
        points.map { latLng -> builder.include(latLng.value?.position) }
        return builder.build()
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