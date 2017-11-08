package ru.a1024bits.bytheway.ui.fragments

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.fragment_maps.*
import ru.a1024bits.aviaanimation.ui.util.LatLngInterpolator
import ru.a1024bits.aviaanimation.ui.util.MarkerAnimation
import ru.a1024bits.bytheway.R
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by andrey.gusenkov on 30/09/2017.
 */
class MapFragment : Fragment(), OnMapReadyCallback {
    
    private lateinit var mMap: GoogleMap
    private var mMapView: MapView? = null
    
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_maps, container, false)
        
        collapsingToolbar?.setContentScrimColor(getResources().getColor(R.color.colorAccent))
        
        mMapView = view.findViewById<MapView>(R.id.map)
        
        mMapView?.onCreate(savedInstanceState)
        
        mMapView?.onResume()
        
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
        
    }
    
    
    var marker: Marker? = null
    override fun onMapReady(googleMap: GoogleMap) {
        this.mMap = googleMap
        val constLocation = LatLng(50.0, 50.0);
        
        mMap.moveCamera(CameraUpdateFactory.newLatLng(constLocation));
        
        mMap.animateCamera(CameraUpdateFactory.zoomTo(3F));
        
    }
    
    fun goFlyPlan() {
        val endLocation = LatLng(32.0, 10.0) // Whatever origin coordinates
        val fromLocation = LatLng(22.00, 10.00)
        
        val markerOptions = MarkerOptions().position(fromLocation).anchor(0.5F, 1.0F).flat(true);
        
        
        var t: Double = 0.0
        while (t < 1.000001) {
            listPointPath.add(LatLngInterpolator.CurveBezie().calculateBezierFunction(t, fromLocation, endLocation))
            t += 0.01F
        }
        drawPolyLineOnMap(listPointPath)
        // Changing marker icon
        markerOptions.icon(bitmapDescriptorFromVector(activity, R.drawable.plane)).rotation(getBearing(listPointPath.first(), listPointPath[1]))
        
        marker = mMap.addMarker(markerOptions);
        animateMarker()
        // - delete after
        val d = (Math.abs(endLocation.latitude) - Math.abs(fromLocation.latitude)) / 2
        val c = (Math.abs(endLocation.longitude) - Math.abs(fromLocation.longitude)) / 2
        
        var point2: LatLng
        var point3: LatLng
        
        if (Math.abs(c) > Math.abs(d)) {
            point2 = LatLng(endLocation.latitude + c * (3 / d), endLocation.longitude - c)
            point3 = LatLng(fromLocation.latitude - c * (2 / d), fromLocation.longitude + c)
        } else {
            point2 = LatLng(endLocation.latitude - d, endLocation.longitude + d * (3 / d))
            point3 = LatLng(fromLocation.latitude + d, fromLocation.longitude - d * (3 / d))
        }
        Log.i("LOG", point3.toString())
        Log.i("LOG", point2.toString())
        
        mMap.addMarker(MarkerOptions().position(point2).title("point2"))
        mMap.addMarker(MarkerOptions().position(point3).title("point3"))
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
            markerAnimation.animateMarker(marker, listPointPath.first(), listPointPath.last(), LatLngInterpolator.CurveBezie(), listPointPath)
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
        mMap.addPolyline(polyOptions)
        
    }
    
    override fun onResume() {
        super.onResume()
        mMapView?.onResume()
    }
    
}