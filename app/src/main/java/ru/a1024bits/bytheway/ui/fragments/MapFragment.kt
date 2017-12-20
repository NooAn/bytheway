package ru.a1024bits.bytheway.ui.fragments

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_maps.*
import kotlinx.android.synthetic.main.fragment_search_block.*
import kotlinx.android.synthetic.main.searching_parameters_block.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.a1024bits.aviaanimation.ui.util.LatLngInterpolator
import ru.a1024bits.aviaanimation.ui.util.MarkerAnimation
import ru.a1024bits.bytheway.App
import ru.a1024bits.bytheway.MapWebService
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.model.map_directions.RoutesList
import ru.a1024bits.bytheway.router.Screens
import ru.a1024bits.bytheway.ui.activity.MenuActivity
import ru.a1024bits.bytheway.util.createMarker
import ru.a1024bits.bytheway.util.toJsonString
import ru.a1024bits.bytheway.viewmodel.DisplayUsersViewModel
import ru.a1024bits.bytheway.viewmodel.MapViewModel
import ru.terrakok.cicerone.commands.Forward
import ru.terrakok.cicerone.commands.Replace
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


/**
 * Created by andrey.gusenkov on 30/09/2017
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

        mMapView?.onCreate(savedInstanceState)

        try {
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

    override fun onDestroy() {
        super.onDestroy()
        mMapView?.onDestroy()
        mMapView = null
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView?.onLowMemory()
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val params = appBarLayout.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = AppBarLayout.Behavior()
        behavior.setDragCallback(object : AppBarLayout.Behavior.DragCallback() {
            override fun canDrag(appBarLayout: AppBarLayout): Boolean = false
        })
        params.behavior = behavior
        appBarLayout.layoutParams = params


        mapFragmentRoot.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                mapFragmentRoot.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val targetMapHeight = mapFragmentRoot.height - resources.getDimensionPixelSize(R.dimen.chooseDestinationLayoutHeight)
                val mapParams = map.layoutParams

                mapParams.height = targetMapHeight
                map.layoutParams = mapParams

                mapFragmentScrollView.scrollTo(0, 0)
            }
        })

        buttonSaveTravelInfo.setOnClickListener {
            //send data to Firebase
            routeString?.let { route -> viewModel?.sendUserData(getHashMapUser(route), uid) }
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

            if (error != 0) {
                Toast.makeText(this@MapFragment.context, getString(error), Toast.LENGTH_SHORT).show()
            } else {
                goFlyPlan()
            }
        }
    }

    var marker: Marker? = null

    override fun onMapReady(googleMap: GoogleMap) {
        this.mMap = googleMap
        val constLocation = LatLng(50.0, 50.0)
        mMap?.moveCamera(CameraUpdateFactory.newLatLng(constLocation))
        mMap?.animateCamera(CameraUpdateFactory.zoomTo(3F))
    }

    fun goFlyPlan() {
        if (points.size < 2) return

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
        animateMarker()
    }

    //lat = y
    //lon = x

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

    val markerAnimation = MarkerAnimation()

    fun animateMarker() {
        // Whatever destination coordinates
        if (markerAnimation.flag == false)
            markerAnimation.animateMarker(marker, listPointPath.first(), listPointPath.last(),
                    LatLngInterpolator.CurveBezie(), listPointPath,
                    onAnimationEnd = {
                        viewModel?.similarUsersLiveData?.observe(this@MapFragment, android.arch.lifecycle.Observer<List<User>> { list ->
                            (activity as MenuActivity).navigator.applyCommand(Forward(Screens.SIMILAR_TRAVELS_SCREEN, list))

                        })
                        viewModel?.getUsersWithSimilarTravel(text_from_city.text.toString(), text_to_city.text.toString())
                    })
    }

    private fun initBoxInputFragment() {
        val searchFragment = SearchFragment()
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
        //  mMap.clear()
        mMap?.addPolyline(polyOptions)

    }

    override fun onResume() {
        super.onResume()
        mMapView?.onResume()
    }

    fun setMarker(point: LatLng, position: Int) {
        mMap?.clear()

        Log.e("LOg", point.toString() + " " + position)
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
                mMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(createLatLngBounds(points),
                        resources.getDimensionPixelSize(R.dimen.latLngBoundsPadding)))
                obtainDirection()
            }
        }
    }

    private fun createLatLngBounds(points: ArrayMap<Int, MarkerOptions>): LatLngBounds {
        val builder = LatLngBounds.builder()
        points.map { latLng -> builder.include(latLng.value?.position) }
        return builder.build()
    }

    fun getHashMapUser(route: String): HashMap<String, Any> {
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