package ru.a1024bits.bytheway.ui.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_user_profile.*
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.router.OnFragmentInteractionListener
import ru.a1024bits.bytheway.viewmodel.UserProfileViewModel


class UserProfileFragment : Fragment(), OnMapReadyCallback {

    private var viewModel: UserProfileViewModel? = null

    private var mListener: OnFragmentInteractionListener? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (arguments != null) {
            val userId: String = arguments.getString(UID_KEY, "")
            viewModel = ViewModelProviders.of(this).get(UserProfileViewModel::class.java)
            viewModel?.init(userId)
            viewModel?.user?.observe(this, Observer<User> {

            })
        }
    }

    @LayoutRes
    protected fun getLayoutRes(): Int {
        return R.layout.fragment_user_profile
    }

    protected fun getViewModelClass(): Class<UserProfileViewModel> {
        return UserProfileViewModel::class.java
    }

    override fun onResume() {
        super.onResume()
        mMapView?.onResume()
    }

    override fun onMapReady(map: GoogleMap?) {
        if (googleMap != null)
            this.googleMap = map
        // Zooming to the Campus location
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(CENTRE, ZOOM))
    }

    private var mMapView: MapView? = null
    private var googleMap: GoogleMap? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_user_profile, container, false)

        mMapView = view?.findViewById<MapView>(R.id.mapView)
        try {
            mMapView?.onCreate(savedInstanceState)
            mMapView?.onResume()// needed to get the map to display immediately
            MapsInitializer.initialize(activity.applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        settingsSocialNetworkButtons()

        mMapView?.getMapAsync(this)

        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        whatsAppIcon.setOnClickListener { startActivity(createBrowserIntent("whatsapp://send?text=Привет, я нашел тебя в ByTheWay.&phone=+numberPhone&abid=+numberPhone")) }

        vkIcon.setOnClickListener {
            val linkUsers = "" //todo fill user links
            startActivity(createBrowserIntent("https://vk.com/$linkUsers"))
        }

        csIcon.setOnClickListener { startActivity(createBrowserIntent("https://www.couchsurfing.com/people/selcukatesoglu")) }
    }

    private fun createBrowserIntent(url: String): Intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

    private fun settingsSocialNetworkButtons() {

    }

    fun onButtonPressed() {
        mListener?.onFragmentInteraction()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context?.toString() + " must implement OnFragmentInteractionListener")
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
        private val UID_KEY = "uid"
        val CENTRE: LatLng = LatLng(-23.570991, -46.649886)
        val ZOOM = 9f

        fun newInstance(uid: String): UserProfileFragment {
            val fragment = UserProfileFragment()
            val args = Bundle()
            args.putString(UID_KEY, uid)
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor

