package ru.a1024bits.bytheway.ui.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.crash.FirebaseCrash
import com.google.maps.android.PolyUtil
import kotlinx.android.synthetic.main.fragment_user_profile.*
import kotlinx.android.synthetic.main.profile_main_image.*
import kotlinx.android.synthetic.main.profile_user_many_direction.*
import kotlinx.android.synthetic.main.profilte_user_direction.*
import ru.a1024bits.bytheway.App
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.model.*
import ru.a1024bits.bytheway.router.OnFragmentInteractionListener
import ru.a1024bits.bytheway.ui.activity.MenuActivity
import ru.a1024bits.bytheway.ui.dialogs.SocNetworkdialog
import ru.a1024bits.bytheway.util.Constants
import ru.a1024bits.bytheway.util.Constants.END_DATE
import ru.a1024bits.bytheway.util.Constants.FIRST_INDEX_CITY
import ru.a1024bits.bytheway.util.Constants.LAST_INDEX_CITY
import ru.a1024bits.bytheway.util.Constants.START_DATE
import ru.a1024bits.bytheway.util.Constants.TWO_DATE
import ru.a1024bits.bytheway.util.Constants.TWO_INDEX_CITY
import ru.a1024bits.bytheway.util.getBearing
import ru.a1024bits.bytheway.viewmodel.UserProfileViewModel
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class UserProfileFragment : BaseFragment<UserProfileViewModel>(), OnMapReadyCallback {

    private var mListener: OnFragmentInteractionListener? = null
    private val userLoad: Observer<Response<User>> = Observer { response ->
        when (response?.status) {
            Status.SUCCESS -> if (response.data == null) showErrorLoading() else fillProfile(response.data)

            Status.ERROR -> {
                showErrorLoading()
            }
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private var mMapView: MapView? = null
    private var googleMap: GoogleMap? = null

    companion object {
        const val TAG_ANALYTICS: String = "UserProfile_"
        private const val UID_KEY = "uid"

        fun newInstance(uid: String): UserProfileFragment {
            val fragment = UserProfileFragment()
            val args = Bundle()
            args.putString(UID_KEY, uid)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component.inject(this)
    }

    private fun fillProfile(user: User) {

        setMarkers(user)
        drawPolyline(user.route)

        username.text = StringBuilder().append(user.name).append(" ").append(user.lastName)

        travelledStatistics.visibility = if (user.flightHours == 0L) View.GONE else View.VISIBLE

        travelledCountries.text = user.countries.toString()
        flightHours.text = user.flightHours.toString()
        flightDistance.text = user.kilometers.toString()

        if (user.city.isNotEmpty()) {
            cityview.text = user.city
        }

        if (user.cities.size == 1 || user.cities.size == 2) {
            textCityFrom.text = user.cities[FIRST_INDEX_CITY]
            textCityTo.text = user.cities[LAST_INDEX_CITY]
        } else if (user.cities.size == 3) {
            direction2.visibility = View.VISIBLE
            three_dots2.visibility = View.GONE
            three_dots1.visibility = View.GONE
            three_dots2_one.visibility = View.VISIBLE
            three_dots1_one.visibility = View.VISIBLE
            textCityFrom.text = user.cities[FIRST_INDEX_CITY]
            textCityTo.text = user.cities[TWO_INDEX_CITY]
            textCityFrom2.text = user.cities[TWO_INDEX_CITY]
            textCityTo2.text = user.cities[LAST_INDEX_CITY]
        }

        val formatDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        if (user.dates.size == 1 || user.dates.size == 2) {
            val dayBegin = formatDate.format(Date(user.dates[Constants.START_DATE] ?: 0))
            val dayArrival = formatDate.format(Date(user.dates[Constants.END_DATE] ?: 0))

            correctText(dayBegin, textDateFrom, iconDateFromEmpty)
            correctText(dayArrival, textDateArrived, iconDateArrivedEmpty)

        } else if (user.dates.size == 0 || (user.dates[START_DATE]?.compareTo(0) == 0 && user.dates[END_DATE]?.compareTo(0) == 0)) {
            textDateEmpty.visibility = View.VISIBLE
            textDateEmpty2.visibility = View.VISIBLE
        } else if (user.dates.size == 3) {
            val dayBegin = formatDate.format(Date(user.dates[Constants.START_DATE] ?: 0))
            val middleDay = formatDate.format(Date(user.dates[Constants.TWO_DATE] ?: 0))
            val dayArrival = formatDate.format(Date(user.dates[Constants.END_DATE] ?: 0))

            correctText(dayBegin, textDateFrom, iconDateFromEmpty)
            correctText(middleDay, textDateArrived, iconDateArrivedEmpty)
            correctText(dayArrival, textDateArrived2, iconDateArrivedEmpty2)
            correctText(middleDay, textDateFrom2, iconDateFromEmpty2)

        }
        if (user.dates[TWO_DATE]?.compareTo(0) == 0 && user.dates[END_DATE]?.compareTo(0) == 0) {
            textDateEmpty2.visibility = View.VISIBLE
        }

        iconDateArrivedEmpty.setOnClickListener {
            clickForIconDateEmpty()
        }
        iconDateFromEmpty.setOnClickListener {
            clickForIconDateEmpty()
        }

        fillAgeSex(user.age, user.sex)
        if (user.urlPhoto.isNotBlank())
            glide?.load(user.urlPhoto)
                    ?.apply(RequestOptions.circleCropTransform())
                    ?.into(image_avatar)
        else
            image_avatar.setImageResource(R.drawable.default_avatar)

        for (name in user.socialNetwork) {
            when (name.key) {
                SocialNetwork.VK.link -> {
                    vkIcon.setImageResource(R.drawable.ic_vk_color)
                    vkIcon.setOnClickListener {
                        try {
                            mFirebaseAnalytics.logEvent(TAG_ANALYTICS + "OPEN_VK", null)
                            startActivity(createBrowserIntent(user.socialNetwork[name.key]
                                    ?: ""))
                        } catch (e: Exception) {
                            e.printStackTrace()
                            FirebaseCrash.report(e)
                            showErrorFroWrongSocValue(user, name)
                        }
                    }
                }
                SocialNetwork.CS.link -> {
                    csIcon.setImageResource(R.drawable.ic_cs_color)
                    csIcon.setOnClickListener {
                        try {
                            startActivity(createBrowserIntent(user.socialNetwork[name.key]
                                    ?: ""))
                            mFirebaseAnalytics.logEvent(TAG_ANALYTICS + "OPEN_CS", null)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            FirebaseCrash.report(e)
                            showErrorFroWrongSocValue(user, name)
                        }
                    }
                }
                SocialNetwork.FB.link -> {
                    fbcon.setImageResource(R.drawable.ic_fb_color)
                    fbcon.setOnClickListener {
                        mFirebaseAnalytics.logEvent(TAG_ANALYTICS + "OPEN_FB", null)
                        try {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("${user.socialNetwork[name.key]}")))
                        } catch (e: Exception) {
                            e.printStackTrace()
                            showErrorFroWrongSocValue(user, name)
                        }
                    }
                }
                SocialNetwork.WHATSAPP.link -> {
                    whatsAppIcon.setImageResource(R.drawable.ic_whats_icon_color)
                    whatsAppIcon.setOnClickListener {
                        mFirebaseAnalytics.logEvent(TAG_ANALYTICS + "OPEN_WAP", null)
                        try {
                            startActivity(createBrowserIntent("whatsapp://send?text=Привет, я нашел тебя в ByTheWay.&phone=+${user.socialNetwork[name.key]}&abid = +${user.socialNetwork[name.key]})}"))
                        } catch (e: Exception) {
                            e.printStackTrace()
                            showErrorFroWrongSocValue(user, name)
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
        if (user.socialNetwork.isEmpty() && user.email.contains("@")) {
            //tgIcon there is will be Email field
            whatsAppIcon.visibility = View.INVISIBLE
            vkIcon.visibility = View.INVISIBLE
            csIcon.visibility = View.INVISIBLE
            fbcon.visibility = View.INVISIBLE
            line1.visibility = View.INVISIBLE
            line4.visibility = View.INVISIBLE
            emailIcon.visibility = View.VISIBLE
            tgIcon.visibility = View.GONE
            emailIcon.setOnClickListener {
                openEmail(user.email)
            }
        }
        for (method in user.method.keys) {
            when (method) {
                Method.CAR.link -> {
                    directions_car.visibility = if (user.method[method] == true) View.VISIBLE else View.GONE
                }
                Method.TRAIN.link -> {
                    directions_railway.visibility = if (user.method[method] == true) View.VISIBLE else View.GONE
                }
                Method.BUS.link -> {
                    directions_bus.visibility = if (user.method[method] == true) View.VISIBLE else View.GONE
                }
                Method.PLANE.link -> {
                    directions_flight.visibility = if (user.method[method] == true) View.VISIBLE else View.GONE
                }
                Method.HITCHHIKING.link -> {
                    direction_hitchiking.visibility = if (user.method[method] == true) View.VISIBLE else View.GONE
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
        userLastTime.text = if (user.timestamp != null) formatDate.format(user.timestamp) else getString(R.string.very_long)
    }

    private fun correctText(day: String, textView: TextView, icon: View) {
        if (day.isNotBlank() && day.isNotEmpty() && day != "01.01.1970")
            textView.text = day
        else icon.visibility = View.VISIBLE
    }

    private fun openEmail(email: String) {
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.setType("message/rfc822")
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf(email))
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.app_name))
        emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (emailIntent.resolveActivity(activity.packageManager) == null) {
            val dialog = SocNetworkdialog(activity, email)
            dialog.show()
        } else
            try {
                context.startActivity(Intent.createChooser(emailIntent, getString(R.string.email_send)))
            } catch (e: Exception) {
                Toast.makeText(activity, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
    }

    private fun showErrorFroWrongSocValue(user: User, name: MutableMap.MutableEntry<String, String>) {
        Toast.makeText(activity.applicationContext, R.string.error_link_open, Toast.LENGTH_SHORT).show()
        val dialog = SocNetworkdialog(activity, user.socialNetwork[name.key])
        dialog.show()
    }

    private fun clickForIconDateEmpty() {
        Toast.makeText(activity, R.string.this_date_is_not_set, Toast.LENGTH_LONG).show()
    }

    private fun fillAgeSex(userAge: Int, userSex: Int) {
        val gender = when (userSex) {
            1 -> getString(R.string.gender_male)
            2 -> getString(R.string.gender_female)
            else -> {
                ""
            }
        }

        if (userSex != 0) {
            if (userAge > 0) {
                sexAndAge.text = StringBuilder(gender).append(", ").append(userAge)
            } else {
                sexAndAge.text = StringBuilder(gender).append(", ${getString(R.string.immortal)} ")
            }
        }

        if (userAge > 0) {
            if (userSex != 0) {
                sexAndAge.text = StringBuilder(gender).append(", ").append(userAge)
            } else {
                sexAndAge.text = StringBuilder(getString(R.string.sex)).append(", ").append(userAge)
            }
        }
        if (userAge == 0 && userSex == 0) {
            sexAndAge.text = getString(R.string.immortal)
        }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel?.loadingStatus?.observe(this, (activity as MenuActivity).progressBarLoad)
        viewModel?.response?.observe(this, userLoad)
    }

    private fun showErrorLoading() {
        Toast.makeText(activity, getString(R.string.error_upload), Toast.LENGTH_SHORT).show()
    }

    override fun getViewFactoryClass(): ViewModelProvider.Factory = viewModelFactory

    @LayoutRes
    override fun getLayoutRes(): Int {
        return R.layout.fragment_user_profile
    }

    override fun getViewModelClass(): Class<UserProfileViewModel> = UserProfileViewModel::class.java


    override fun onResume() {
        super.onResume()
        try {
            mMapView?.onResume()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onMapReady(map: GoogleMap?) {
        this.googleMap = map

        if (arguments != null) {
            val userId: String = arguments.getString(UID_KEY, "")
            viewModel?.load(userId)
        } else {
            showErrorLoading()
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        mMapView = view?.findViewById(R.id.mapView)
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

    override fun onDetach() {
        mListener = null
        super.onDetach()
    }

    private fun setMarkers(user: User) {
        googleMap?.clear()
        val markerPositionFinal = LatLng(user.cityFromLatLng.latitude, user.cityFromLatLng.longitude)
        val markerPositionStart = LatLng(user.cityToLatLng.latitude, user.cityToLatLng.longitude)

        val markerTitleStart = user.cities[Constants.LAST_INDEX_CITY]
        val markerTitleFinal = user.cities[Constants.FIRST_INDEX_CITY]

        val midPointLat = (markerPositionFinal.latitude + markerPositionStart.latitude) / 2
        val midPointLong = (markerPositionFinal.longitude + markerPositionStart.longitude) / 2
        val blueMarker = BitmapDescriptorFactory.fromResource(R.drawable.pin_blue)

        if (user.cityTwoLatLng.latitude != 0.0 && user.cityTwoLatLng.longitude != 0.0) {
            googleMap?.addMarker(MarkerOptions()
                    .icon(blueMarker)
                    .position(LatLng(user.cityTwoLatLng.latitude, user.cityTwoLatLng.longitude))
                    .title(user.cities[Constants.TWO_INDEX_CITY])
                    .anchor(0.5F, 1.0F)
                    .flat(true))
        }
        googleMap?.addMarker(MarkerOptions()
                .icon(blueMarker)
                .position(markerPositionStart)
                .title(markerTitleStart)
                .anchor(0.5F, 1.0F)
                .flat(true))

        googleMap?.addMarker(MarkerOptions()
                .icon(blueMarker)
                .position(markerPositionFinal)
                .title(markerTitleFinal)
                .anchor(0.5F, 1.0F)
                .flat(true))

        val perfectZoom = 190 / markerPositionFinal.getBearing(markerPositionStart) - 1

        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(midPointLat, midPointLong), perfectZoom))
    }

    private fun drawPolyline(routeString: String) {
        val blueColor = ContextCompat.getColor(context, R.color.blueRouteLine)
        val options = PolylineOptions()
        options.color(blueColor)
        options.width(5f)
        if (routeString.isNotBlank()) options.addAll(PolyUtil.decode(routeString))
        googleMap?.addPolyline(options)
    }

    private fun intentMessageTelegram(id: String?) {
        try {
            if (id?.isNumberPhone() == false && id?.startsWith("@") == true) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/${id.replace("@", "")}")))
                mFirebaseAnalytics.logEvent(TAG_ANALYTICS + "OPEN_TG", null)
            } else {
                mFirebaseAnalytics.logEvent(TAG_ANALYTICS + "NOT_OPEN_TG", null)
                val dialog = SocNetworkdialog(activity, id)
                dialog.show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            FirebaseCrash.report(e)
            Toast.makeText(activity.applicationContext, R.string.error_link_open, Toast.LENGTH_SHORT).show()
            val dialog = SocNetworkdialog(activity, "https://t.me/${id?.replace("@", "")}")
            dialog.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            mapView?.onDestroy()
            //Clean up resources from google map to prevent memory leaks.
            //Stop tracking current location
            if (googleMap != null) {
                googleMap?.clear()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView?.onLowMemory()
        mListener = null
    }
}// Required empty public constructor

fun String.isNumberPhone(): Boolean {
    return this.matches(Regex("^([0-9]|\\+[0-9]){11,13}\$"))
    //this.startsWith("+7")

}


