package ru.a1024bits.bytheway.ui.fragments

import android.arch.lifecycle.LifecycleFragment
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.router.OnFragmentInteractionListener
import ru.a1024bits.bytheway.viewmodel.UserProfileViewModel


class UserProfileFragment : LifecycleFragment(), OnMapReadyCallback {
    
    
    private var viewModel: UserProfileViewModel? = null
    
    private var mListener: OnFragmentInteractionListener? = null
    
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
    }
    
    
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (arguments != null) {
            val userId: String = arguments.getString(UID_KEY)
            viewModel = ViewModelProviders.of(this).get(UserProfileViewModel::class.java)
            viewModel?.init(userId)
            viewModel?.user?.observe(this, Observer {
            
            })
        }
    }
    
    override fun onResume() {
        super.onResume()
        mMapView?.onResume()
    }
    
    override fun onMapReady(map: GoogleMap?) {
        if (googleMap != null)
            this.googleMap = map
        
        googleMap?.addMarker(MarkerOptions().position(CENTRE).title("Hello, Dude!"))
        
        // Zooming to the Campus location
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(CENTRE, ZOOM))
    }
    
    private var mMapView: MapView? = null
    private var googleMap: GoogleMap? = null
    
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_user_profile, container, false)
        
        mMapView = view?.findViewById<MapView>(R.id.mapView)
        mMapView?.onCreate(savedInstanceState)
        
        mMapView?.onResume()// needed to get the map to display immediately
        
        try {
            MapsInitializer.initialize(activity.applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        settingsSocialNetworkButtons()
        
        mMapView?.getMapAsync(this)
        
        // latitude and longitude
        val latitude = 17.385044
        val longitude = 78.486671
        
        // create marker
        val marker = MarkerOptions().position(
                LatLng(latitude, longitude)).title("Hello Maps")
        return view;
    }
    
    private fun settingsSocialNetworkButtons() {

    }
    
    fun onButtonPressed() {
        if (mListener != null) {
            mListener!!.onFragmentInteraction()
        }
    }
    
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }
    
    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        mMapView?.onSaveInstanceState(outState)
    }
    
    override fun onDetach() {
        super.onDetach()
        mListener = null
    }
    
    override fun onPause() {
        super.onPause()
        mMapView?.onPause()
    }
    
    override fun onStart() {
        super.onStart()
        mMapView?.onStart()
    }
    
    override fun onStop() {
        super.onStop()
        mMapView?.onStop()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mMapView?.onDestroy()
    }
    
    override fun onLowMemory() {
        super.onLowMemory()
        mMapView?.onLowMemory()
    }
    

    
    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private val ARG_PARAM1 = "param1"
        private val UID_KEY = "uid"
        val CENTRE: LatLng = LatLng(-23.570991, -46.649886)
        val ZOOM = 9f
        
        fun newInstance(param1: String, param2: String): UserProfileFragment {
            val fragment = UserProfileFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
