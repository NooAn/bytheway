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
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_maps.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.a1024bits.aviaanimation.ui.util.LatLngInterpolator
import ru.a1024bits.aviaanimation.ui.util.MarkerAnimation
import ru.a1024bits.bytheway.App
import ru.a1024bits.bytheway.MapWebService
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.model.map_directions.RoutesList
import ru.a1024bits.bytheway.router.Screens
import ru.a1024bits.bytheway.ui.activity.MenuActivity
import ru.a1024bits.bytheway.util.createMarker
import ru.a1024bits.bytheway.util.toJsonString
import ru.a1024bits.bytheway.viewmodel.MapViewModel
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

    private var viewModel: MapViewModel? = null
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    private val uid: String by lazy { FirebaseAuth.getInstance().currentUser?.uid.orEmpty() }

    @Inject lateinit var mapService: MapWebService

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        App.component.inject(this)

        //todo: work with this
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MapViewModel::class.java)
        viewModel?.load?.observe(this, android.arch.lifecycle.Observer {
            Log.e("LOG", "observer map fragment")
        })
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
            goFlyPlan()
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
        // - delete after

        val lat1 = fromLocation.latitude
        val lat2 = endLocation.latitude

        val lon1 = fromLocation.longitude
        val lon2 = endLocation.longitude

        val angle = findArctg(lat1, lat2, lon1, lon2)
        val module = module(lat1, lat2, lon1, lon2)

        val latCentral = (lat2 + lat1) / 2
        val lonCentral = (lon2 + lon1) / 2

        val latTop = latCentral + module / 4
        val lonTop = lonCentral

        val latBottom = latCentral - module / 4
        val lonBottom = lonCentral

        val rotatedTop = rotatePoint(lonTop, latTop, lonCentral, latCentral, Math.toRadians(angle))
        val rotatedBottom = rotatePoint(lonBottom, latBottom, lonCentral, latCentral, Math.toRadians(angle))

        val point2 = LatLng(rotatedTop[1], rotatedTop[0])
        val point3 = LatLng(rotatedBottom[1], rotatedBottom[0])

        Log.i("LOG", point3.toString())
        Log.i("LOG", point2.toString())

        /*mMap?.addMarker(MarkerOptions().position(point2).title("point2"))
        mMap?.addMarker(MarkerOptions().position(point3).title("point3"))*/
    }

    //lat = y
    //lon = x

    private fun findArctg(lat1: Double, lat2: Double, lon1: Double, lon2: Double): Double {
        val arctg = Math.atan((lat2 - lat1) / (lon2 - lon1))
        return Math.toDegrees(arctg)
    }

    /* Apply rotation matrix to the point(x,y) */
    private fun rotatePoint(x: Double, y: Double, x0: Double, y0: Double, angle: Double): Array<Double> {
        val x1 = -(y - y0) * Math.sin(angle) + Math.cos(angle) * (x - x0) + x0
        val y1 = (y - y0) * +Math.cos(angle) + Math.sin(angle) * (x - x0) + y0
        return arrayOf(x1, y1)
    }

    private fun module(lat1: Double, lat2: Double, lon1: Double, lon2: Double): Double {
        return Math.sqrt((lat2 - lat1) * (lat2 - lat1) + (lon2 - lon1) * (lon2 - lon1))
    }

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
        Log.e("LOG", "animate marker last and size: ${listPointPath.last()} ${listPointPath.size}")
        // Whatever destination coordinates
        if (markerAnimation.flag == false)
            markerAnimation.animateMarker(marker, listPointPath.first(), listPointPath.last(),
                    LatLngInterpolator.CurveBezie(), listPointPath,

                    onAnimationEnd = {
                        (activity as MenuActivity).navigator
                                .applyCommand(Replace(
                                        Screens.SIMILAR_TRAVELS_SCREEN, 1))
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
                        Log.w("myLogs", "uid: $uid; encoded direction route: $routeString")
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