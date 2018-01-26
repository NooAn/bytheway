package ru.a1024bits.bytheway.ui.fragments

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.fragment_user_profile.*
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.router.OnFragmentInteractionListener
import ru.a1024bits.bytheway.viewmodel.UserProfileViewModel
import android.widget.Toast
import android.arch.lifecycle.ViewModelProvider
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.util.Log
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import kotlinx.android.synthetic.main.profilte_user_direction.*
import kotlinx.android.synthetic.main.profile_main_image.*
import ru.a1024bits.bytheway.App
import ru.a1024bits.bytheway.model.*
import ru.a1024bits.bytheway.ui.activity.MenuActivity
import ru.a1024bits.bytheway.ui.dialogs.SocNetworkdialog
import ru.a1024bits.bytheway.util.Constants
import ru.a1024bits.bytheway.util.getBearing
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class UserProfileFragment : BaseFragment<UserProfileViewModel>(), OnMapReadyCallback {

    private var mListener: OnFragmentInteractionListener? = null
    private val userLoad: Observer<Response<User>> = Observer<Response<User>> { response ->
        when (response?.status) {
            Status.SUCCESS -> if (response.data == null) showErrorLoading() else fillProfile(response.data)

            Status.ERROR -> {
                Log.e("LOG", "log e:" + response.error)
                showErrorLoading()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component.inject(this)
    }

    private val TAG_ANALYTICS: String = "UserProfile_"

    private fun fillProfile(user: User) {

        setMarkers(user)
        showRouteOnMap(user.route)

        username.text = StringBuilder().append(user.name).append(" ").append(user.lastName)

        travelledStatistics.visibility = if (user.flightHours == 0L) View.GONE else View.VISIBLE

        travelledCountries.text = user.countries.toString()
        flightHours.text = user.flightHours.toString()
        flightDistance.text = user.kilometers.toString()

        if (user.city.length > 0) {
            cityview.text = user.city
        }

        if (user.cities.size > 0) {
            textCityFrom.text = user.cities.get(Constants.FIRST_INDEX_CITY)
            textCityTo.text = user.cities.get(Constants.LAST_INDEX_CITY)
        }

        val formatDate = SimpleDateFormat("dd.MM.yyyy", Locale.US)

        if (user.dates.size > 0) {
            val dayBegin = formatDate.format(Date(user.dates.get(Constants.START_DATE) ?: 0))
            val dayArrival = formatDate.format(Date(user.dates.get(Constants.END_DATE) ?: 0))
            if (dayBegin.isNotBlank() && dayBegin.length > 0 && !dayBegin.equals("01.01.1970")) textDateFrom.setText(dayBegin) else iconDateFromEmpty.visibility = View.VISIBLE
            if (dayArrival.isNotBlank() && dayArrival.length > 0 && !dayArrival.equals("01.01.1970")) dateArrived.setText(dayArrival) else iconDateArrivedEmpty.visibility = View.VISIBLE
        } else {
            textDateEmpty.visibility = View.VISIBLE
        }
        iconDateArrivedEmpty.setOnClickListener { clickForIconDateEmpty() }
        iconDateFromEmpty.setOnClickListener { clickForIconDateEmpty() }

        fillAgeSex(user.age, user.sex)

        glide?.load(user.urlPhoto)
                ?.apply(RequestOptions.circleCropTransform())
                ?.into(image_avatar)

        for (name in user.socialNetwork) {
            when (name.key) {
                SocialNetwork.VK.link -> {
                    vkIcon.setImageResource(R.drawable.ic_vk_color)
                    vkIcon.setOnClickListener {
                        startActivity(createBrowserIntent(user.socialNetwork.get(name.key) ?: ""))
                        mFirebaseAnalytics.logEvent(TAG_ANALYTICS + "OPEN_VK", null)
                    }
                }
                SocialNetwork.CS.link -> {
                    csIcon.setImageResource(R.drawable.ic_cs_color)
                    csIcon.setOnClickListener {
                        startActivity(createBrowserIntent(user.socialNetwork.get(name.key) ?: ""))
                        mFirebaseAnalytics.logEvent(TAG_ANALYTICS + "OPEN_CS", null)
                    }
                }
                SocialNetwork.FB.link -> {
                    fbcon.setImageResource(R.drawable.ic_fb_color)
                    fbcon.setOnClickListener {
                        //  startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("facebook:/profile/id=${user.socialNetwork.get(name.key)}")))
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("${user.socialNetwork.get(name.key)}")))
                        mFirebaseAnalytics.logEvent(TAG_ANALYTICS + "OPEN_FB", null)
                    }
                }
                SocialNetwork.WHATSAPP.link -> {
                    whatsAppIcon.setImageResource(R.drawable.ic_whats_icon_color)
                    whatsAppIcon.setOnClickListener {
                        mFirebaseAnalytics.logEvent(TAG_ANALYTICS + "OPEN_WAP", null)
                        try {
                            startActivity(createBrowserIntent("whatsapp://send?text=Привет, я нашел тебя в ByTheWay.&phone=+${user.socialNetwork.get(name.key)}&abid = +${user.socialNetwork.get(name.key)})}"))
                        } catch (e: Exception) {
                            val dialog = SocNetworkdialog(activity, user.socialNetwork.get(name.key))
                            dialog.show()
                            e.printStackTrace()
                        }
                    }
                }
                SocialNetwork.TG.link -> {
                    tgIcon.setImageResource(R.drawable.ic_tg_color)
                    tgIcon.setOnClickListener {
                        intentMessageTelegram(user.socialNetwork[name.key])
                    }
                }
            }
        }
        for (method in user.method.keys) {
            when (method) {
                Method.CAR.link -> {
                    directions_car.visibility = if (user.method.get(method) == true) View.VISIBLE else View.GONE
                }
                Method.TRAIN.link -> {
                    directions_railway.visibility = if (user.method.get(method) == true) View.VISIBLE else View.GONE
                }
                Method.BUS.link -> {
                    directions_bus.visibility = if (user.method.get(method) == true) View.VISIBLE else View.GONE
                }
                Method.PLANE.link -> {
                    directions_flight.visibility = if (user.method.get(method) == true) View.VISIBLE else View.GONE
                }
                Method.HITCHHIKING.link -> {
                    direction_hitchiking.visibility = if (user.method.get(method) == true) View.VISIBLE else View.GONE
                }
            }
        }

        if (!user.method.containsValue(true)) {
            layout_method_moving.visibility = View.GONE
        }

        if (user.budget > 0) {
            displayPriceTravel.text = user.budget.toString()
        } else {
            displayPriceTravel.text = "0"
            moneyForTrip.visibility = View.GONE
        }

        addInfoUser.text = user.addInformation
        if (user.addInformation.isBlank()) descriptionProfile.visibility = View.GONE
    }

    private fun clickForIconDateEmpty() {
        Toast.makeText(activity, R.string.this_date_is_not_set, Toast.LENGTH_LONG).show()
    }

    fun fillAgeSex(userAge: Int, userSex: Int) {
        val gender = when (userSex) {
            1 -> "М"
            2 -> "Ж"
            else -> {
                ""
            }
        }

        if (userSex != 0) {
            if (userAge > 0) {
                sexAndAge.text = StringBuilder(gender).append(", ").append(userAge)
            } else {
                sexAndAge.text = StringBuilder(gender).append(", Бессмертный ").append(userAge)
            }
        }

        if (userAge > 0) {
            if (userSex != 0) {
                sexAndAge.text = StringBuilder(gender).append(", ").append(userAge)
            } else {
                sexAndAge.text = StringBuilder("Пол, ").append(userAge)
            }
        }
    }

    private fun showRouteOnMap(route: String) {
        drawPolyline(route)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel?.loadingStatus?.observe(this, (activity as MenuActivity).progressBarLoad)
        viewModel?.response?.observe(this, userLoad)
    }

    private fun showErrorLoading() {
        Log.e("LOg", "ERROR")
        Toast.makeText(activity, "Ошибка загрузки", Toast.LENGTH_SHORT).show()
    }

    override fun getViewFactoryClass(): ViewModelProvider.Factory = viewModelFactory

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @LayoutRes
    override fun getLayoutRes(): Int {
        return R.layout.fragment_user_profile
    }

    override fun getViewModelClass(): Class<UserProfileViewModel> = UserProfileViewModel::class.java


    override fun onResume() {
        super.onResume()
        mMapView?.onResume()
    }

    override fun onMapReady(map: GoogleMap?) {
        this.googleMap = map
        // Zooming to the Campus location
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(CENTRE, ZOOM))

        if (arguments != null) {
            val userId: String = arguments.getString(UID_KEY, "")
            viewModel?.load(userId)
        } else {
            // fix me error when don't load user
            showErrorLoading()
        }
    }

    private var mMapView: MapView? = null
    private var googleMap: GoogleMap? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        mMapView = view?.findViewById<MapView>(R.id.mapView)
        try {
            mMapView?.onCreate(savedInstanceState)
            mMapView?.onResume()// needed to get the map to display immediately
            MapsInitializer.initialize(activity.applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mMapView?.getMapAsync(this)
        settingsSocialNetworkButtons()

        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun createBrowserIntent(url: String): Intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

    private fun settingsSocialNetworkButtons() {

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
    }

    override fun onDetach() {
        mListener = null
        super.onDetach()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    private fun setMarkers(user: User) {
        googleMap?.clear()
        val coordFrom = LatLng(user.cityFromLatLng.latitude, user.cityFromLatLng.longitude)
        val coordTo = LatLng(user.cityToLatLng.latitude, user.cityToLatLng.longitude)
        var markerTitleStart: String? = ""
        var markerTitleFinal: String? = ""
        var markerPositionStart = LatLng(0.0, 0.0)
        var markerPositionFinal = LatLng(0.0, 0.0)
        val cityFrom = user.cities.get(Constants.FIRST_INDEX_CITY)
        val cityTo = user.cities.get(Constants.LAST_INDEX_CITY)
        markerTitleStart = cityTo
        markerTitleFinal = cityFrom
        markerPositionStart = coordTo
        markerPositionFinal = coordFrom


        val midPointLat = (coordFrom.latitude + coordTo.latitude) / 2
        val midPointLong = (coordFrom.longitude + coordTo.longitude) / 2
        val blueMarker = BitmapDescriptorFactory.fromResource(R.drawable.pin_blue)

        googleMap?.addMarker(MarkerOptions()
                .icon(blueMarker)
                .position(markerPositionStart)
                .title(markerTitleStart)
                .anchor(0.5F, 1.0F)
                .flat(true))

        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(markerPositionFinal, 6.0f))
        googleMap?.addMarker(MarkerOptions()
                .icon(blueMarker)
                .position(markerPositionFinal)
                .title(markerTitleFinal)
                .anchor(0.5F, 1.0F)
                .flat(true))

        val perfectZoom = 190 / coordFrom.getBearing(coordTo)

        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(midPointLat, midPointLong), perfectZoom))
    }

    fun drawPolyline(routeString: String) {
        val blueColor = ContextCompat.getColor(context, R.color.blueRouteLine)
        val options = PolylineOptions()
        options.color(blueColor)
        options.width(5f)
        if (routeString != "") options.addAll(PolyUtil.decode(routeString))
        googleMap?.addPolyline(options)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun intentMessageTelegram(id: String?) {
        if (id?.isNumberPhone() == false) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/${id?.replace("@", "")}")))
            mFirebaseAnalytics.logEvent(TAG_ANALYTICS + "OPEN_TG", null)
        } else {
            mFirebaseAnalytics.logEvent(TAG_ANALYTICS + "NOT_OPEN_TG", null)
            val dialog = SocNetworkdialog(activity, id)
            dialog.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView?.onDestroy()
        //Clean up resources from google map to prevent memory leaks.
        //Stop tracking current location
        if (googleMap != null) {
            googleMap?.clear()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView?.onLowMemory()
        mListener = null
    }


    companion object {
        private val UID_KEY = "uid"
        val CENTRE: LatLng = LatLng(-23.570991, -43.649886)
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

private fun String.isNumberPhone(): Boolean {
    return this.matches(Regex("^([0-9]|\\+[0-9]){11,13}\$")) ||
            this.startsWith("+7") && !this.startsWith("@")
}


