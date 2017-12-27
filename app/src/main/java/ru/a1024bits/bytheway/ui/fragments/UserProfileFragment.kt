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
import android.util.Log
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.profilte_user_direction.*
import kotlinx.android.synthetic.main.profile_main_image.*
import ru.a1024bits.bytheway.App
import ru.a1024bits.bytheway.model.*
import ru.a1024bits.bytheway.util.Constants
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
    private val progressBarLoad: Observer<Boolean> = Observer<Boolean> { b ->
        // fix me after create loader for app
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component.inject(this)
    }

    private fun fillProfile(user: User) {

        showRouteOnMap(user.route)

        username.text = StringBuilder().append(user.name).append(user.lastName)

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

        val formatDate = SimpleDateFormat("dd.MM.yyyy")

        if (user.dates.size > 0) {
            val dayBegin = formatDate.format(Date(user.dates.get(Constants.START_DATE) ?: 0))
            val dayArrival = formatDate.format(Date(user.dates.get(Constants.END_DATE) ?: 0))
            textDateFrom.setText(dayBegin)
            dateArrived.setText(dayArrival)
        }

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
                    }
                }
                SocialNetwork.CS.link -> {
                    csIcon.setImageResource(R.drawable.ic_cs_color)
                    csIcon.setOnClickListener { startActivity(createBrowserIntent(user.socialNetwork.get(name.key) ?: "")) }

                }
                SocialNetwork.FB.link -> {
                    fbcon.setImageResource(R.drawable.ic_fb_color)
                    fbcon.setOnClickListener {
                        //  startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("facebook:/profile/id=${user.socialNetwork.get(name.key)}")))
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("${user.socialNetwork.get(name.key)}")))
                    }
                }
                SocialNetwork.WHATSAPP.link -> {
                    whatsAppIcon.setImageResource(R.drawable.ic_whats_icon_color)
                    whatsAppIcon.setOnClickListener {
                        startActivity(createBrowserIntent("whatsapp://send?text=Привет, я нашел тебя в ByTheWay.&phone=+${user.socialNetwork.get(name.key)}&abid = +${user.socialNetwork.get(name.key)})}"))
                    }
                }
                SocialNetwork.TG.link -> {
                    tgIcon.setImageResource(R.drawable.ic_tg_color)
                    tgIcon.setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://telegram.me/${user.socialNetwork.get(name.key)}"))) }
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

        if (user.budget > 0) {
            displayPriceTravel.text = StringBuilder(getString(R.string.type_money)).append(user.budget)
        }
        add_info_user.text = user.addInformation
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
                sex_and_age.text = StringBuilder(gender).append(", ").append(userAge)
            } else {
                sex_and_age.text = StringBuilder(gender).append(", Возраст ").append(userAge)
            }
        }

        if (userAge > 0) {
            if (userSex != 0) {
                sex_and_age.text = StringBuilder(gender).append(", ").append(userAge)
            } else {
                sex_and_age.text = StringBuilder("Пол, ").append(userAge)
            }
        }
    }

    private fun showRouteOnMap(route: String) {

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel?.response?.observe(this, userLoad)
        viewModel?.loadingStatus?.observe(this, progressBarLoad)

        if (arguments != null) {
            val userId: String = arguments.getString(UID_KEY, "")
            viewModel?.load(userId)
        } else {
            // fix me error when don't load user
            showErrorLoading()
        }
    }

    private fun showErrorLoading() {
        Log.e("LOg", "ERROR")
        Toast.makeText(activity, "Ошибка загрузки", Toast.LENGTH_SHORT).show()
    }

    override fun getViewFactoryClass(): ViewModelProvider.Factory = viewModelFactory

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

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
        mListener = null
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

private val MAX_LENGTH_FOR_SHORT_STRING = 10

private fun String.lastSymbols(): CharSequence? {
    val n = if (this.length > MAX_LENGTH_FOR_SHORT_STRING) MAX_LENGTH_FOR_SHORT_STRING else this.length
    val shortString = this.substring(0, n)
    if (shortString.length == this.length) {
        return this
    } else {
        return shortString + "..."
    }
}

