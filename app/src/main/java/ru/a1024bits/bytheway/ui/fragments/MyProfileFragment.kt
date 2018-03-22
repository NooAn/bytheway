package ru.a1024bits.bytheway.ui.fragments

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.design.widget.NavigationView
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.bumptech.glide.request.RequestOptions
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment
import com.codetroopers.betterpickers.calendardatepicker.MonthAdapter
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crash.FirebaseCrash
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.PolyUtil
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKCallback
import com.vk.sdk.VKSdk
import com.vk.sdk.api.VKError
import kotlinx.android.synthetic.main.confirm_dialog.view.*
import kotlinx.android.synthetic.main.fragment_my_user_profile.*
import kotlinx.android.synthetic.main.profile_add_trip.*
import kotlinx.android.synthetic.main.profile_direction.*
import kotlinx.android.synthetic.main.profile_main_image.*
import ru.a1024bits.bytheway.App
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.model.*
import ru.a1024bits.bytheway.model.map_directions.RoutesList
import ru.a1024bits.bytheway.router.OnFragmentInteractionListener
import ru.a1024bits.bytheway.router.Screens
import ru.a1024bits.bytheway.ui.activity.MenuActivity
import ru.a1024bits.bytheway.ui.dialogs.SocialTipsDialog
import ru.a1024bits.bytheway.util.*
import ru.a1024bits.bytheway.util.Constants.END_DATE
import ru.a1024bits.bytheway.util.Constants.FIRST_INDEX_CITY
import ru.a1024bits.bytheway.util.Constants.LAST_INDEX_CITY
import ru.a1024bits.bytheway.util.Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_FROM
import ru.a1024bits.bytheway.util.Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_FROM_MIDDLE_CITY
import ru.a1024bits.bytheway.util.Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_TO
import ru.a1024bits.bytheway.util.Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_TO_NEW_CITY
import ru.a1024bits.bytheway.util.Constants.START_DATE
import ru.a1024bits.bytheway.util.Constants.TWO_DATE
import ru.a1024bits.bytheway.util.Constants.TWO_INDEX_CITY
import ru.a1024bits.bytheway.viewmodel.MyProfileViewModel
import ru.terrakok.cicerone.commands.Replace
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap
import ru.a1024bits.bytheway.model.Response as ResponseBtw


class MyProfileFragment : BaseFragment<MyProfileViewModel>(), OnMapReadyCallback {

    companion object {
        const val BUDGET = "budget"
        const val BUDGET_POSITION = "budgetPosition"
        const val CITIES = "cities"
        const val METHOD = "method"
        const val METHODS = "methods"
        const val DATES = "dates"
        const val CITY_FROM = "cityFromLatLng"
        const val CITY_TO = "cityToLatLng"
        const val CITY_TWO = "cityTwoLatLng"
        const val ADD_INFO = "addInformation"
        const val COUNT_TRIP = "countTrip"
        const val SEX = "sex"
        const val AGE = "age"
        const val NAME = "name"
        const val LASTNAME = "lastName"
        const val ROUTE = "route"
        const val CITY = "city"
        const val TAG_ANALYTICS: String = "MProfile_screen"
        const val START_BUDGET: Int = 50
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private var mListener: OnFragmentInteractionListener? = null

    private var uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    private var name = ""
    private var cityFromLatLng = GeoPoint(0.0, 0.0)
    private var cityToLatLng = GeoPoint(0.0, 0.0)
    private var cityTwoLatLng = GeoPoint(0.0, 0.0)
    private var lastName = ""
    private var city = ""
    private lateinit var dateDialog: CalendarDatePickerDialogFragment
    private var numberPhone: String = "+7"
    private var socialValues: HashMap<String, String> = hashMapOf(
            SocialNetwork.VK.link to "https://www.vk.com/",
            SocialNetwork.TG.link to "@",
            SocialNetwork.CS.link to "https://www.couchsurfing.com/people/",
            SocialNetwork.FB.link to "https://www.facebook.com/",
            SocialNetwork.WHATSAPP.link to "+7"
    )
    private var defaultSocialValues: HashMap<String, String> = hashMapOf(
            SocialNetwork.VK.link to "https://www.vk.com/",
            SocialNetwork.TG.link to "@",
            SocialNetwork.CS.link to "https://www.couchsurfing.com/people/",
            SocialNetwork.FB.link to "https://www.facebook.com/",
            SocialNetwork.WHATSAPP.link to "+7"
    )

    private var methods: HashMap<String, Boolean> = hashMapOf(
            Method.CAR.link to false,
            Method.TRAIN.link to false,
            Method.BUS.link to false,
            Method.PLANE.link to false,
            Method.HITCHHIKING.link to false
    )
    private var methodIcons: HashMap<String, RelativeLayout> = hashMapOf()
    private var methodTextViews: HashMap<String, TextView?> = hashMapOf()
    /**
     *
     */
    private var socNet: HashMap<String, String> = hashMapOf()
    private var dates: HashMap<String, Long> = hashMapOf()
    private var sex: Int = 0
    private var age: Int = 0
    private var cities: HashMap<String, String> = hashMapOf()
    private var budget: Long = 0
    private var budgetPosition: Int = 0
    private var profileStateHashMap: HashMap<String, String> = hashMapOf()
    private var oldProfileState: Int = 0
    private var routeString: String = ""
    private var googleMap: GoogleMap? = null
    private var mapView: MapView? = null
    private var countTrip: Int = 0

    private val routsObserver: Observer<ResponseBtw<RoutesList>> = Observer { response ->
        when (response?.status) {
            Status.SUCCESS -> {
                if (response.data != null && activity != null) {
                    if (response.data.routes?.size == 0) {
                        this@MyProfileFragment.routeString = ""
                        drawPolyline()
                    }
                    response.data.routes?.map {
                        it.overviewPolyline?.encodedData?.let { routeString ->
                            this@MyProfileFragment.routeString = routeString
                            drawPolyline()
                        }
                    }
                }
            }
            Status.ERROR -> {
                showErrorRout()
            }
        }
    }

    private val photoUrlObserver: Observer<ResponseBtw<String>> = Observer {
        when (it?.status) {
            Status.SUCCESS -> {
                if (it.data != null && !activity.isDestroyed) {
                    updateImageProfile(it.data)
                    mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_upload_image", null)

                }
            }
            Status.ERROR -> {
                mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_error_upload", null)
                showErrorUploadImage()
            }
        }
    }

    private val tokenObserver: Observer<ResponseBtw<Boolean>> = Observer {
        when (it?.status) {
            Status.SUCCESS -> {
                if (!activity.isDestroyed) {
                    (activity as MenuActivity).tokenUpdated()
                    mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_token_updated", null)
                }
            }
            Status.ERROR -> {
                mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_error_token_upd", null)
            }
        }
    }

    private fun showErrorUploadImage() {
        Toast.makeText(activity, R.string.error_upload, Toast.LENGTH_SHORT).show()
    }

    private val usersObservers: Observer<User> = Observer { user ->
        mListener?.onFragmentInteraction(user)
    }


    private val responseObserver: Observer<ResponseBtw<User>> = Observer { response ->
        when (response?.status) {
            Status.SUCCESS -> {
                if (response.data != null) {
                    if (activity != null) {
                        fillProfile(response.data)
                        viewModel?.loadingStatus?.setValue(false)
                        mListener?.onFragmentInteraction(response.data)
                        mainUser = response.data
                    }
                }
            }
            Status.ERROR -> {
                showErrorResponse()
                Log.e("LOG", "log e:" + response.error)
            }
        }
    }

    private val observerSaveProfile: Observer<ResponseBtw<Boolean>> = Observer { response ->
        when (response?.status) {
            Status.SUCCESS -> {
                if (response.data == true && activity != null) {
                    Toast.makeText(this@MyProfileFragment.context, resources.getString(R.string.save_succesfull), Toast.LENGTH_SHORT).show()
                    profileChanged(false)
                } else {
                    showErrorResponse()
                }
            }
            Status.ERROR -> {
                showErrorResponse()
                Log.e("LOG", "log e:" + response.error)
            }
        }
    }

    private val observerSaveSocial: Observer<SocialResponse> = Observer { response ->
        response?.let {
            if (response.value.isEmpty()) {
                changeSocIconsDisActive(response.link)
            } else {
                changeSocIconsActive(response.link, response.value)
            }
        }
    }

    private var mainUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component.inject(this)
    }

    private fun showErrorRout() {
        mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_error_routs", null)
        Log.d("LOG", "error  get routing")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        methodIcons.put(Method.CAR.link, iconCar)
        methodIcons.put(Method.TRAIN.link, iconTrain)
        methodIcons.put(Method.BUS.link, iconBus)
        methodIcons.put(Method.PLANE.link, iconPlane)
        methodIcons.put(Method.HITCHHIKING.link, iconHitchHicking)

        methodTextViews.put(Method.CAR.link, travelCarText)
        methodTextViews.put(Method.TRAIN.link, travelTrainText)
        methodTextViews.put(Method.BUS.link, travelBusText)
        methodTextViews.put(Method.PLANE.link, travelPlaneText)
        methodTextViews.put(Method.HITCHHIKING.link, travelHitchHikingText)

        viewModel?.routes?.observe(this, routsObserver)
        viewModel?.response?.observe(this, responseObserver)
        viewModel?.user?.observe(this, usersObservers)
        viewModel?.loadingStatus?.observe(this, (activity as MenuActivity).progressBarLoad)

        viewModel?.photoUrl?.observe(this, photoUrlObserver)
        viewModel?.token?.observe(this, tokenObserver)

        if (viewModel?.saveSocial?.hasObservers() == false) {
            viewModel?.saveSocial?.observe(this, observerSaveSocial)
        }
        if (viewModel?.saveProfile?.hasObservers() == false) {
            viewModel?.saveProfile?.observe(this, observerSaveProfile)
        }
    }

    override fun getViewFactoryClass(): ViewModelProvider.Factory = viewModelFactory

    @LayoutRes
    override fun getLayoutRes(): Int {
        return R.layout.fragment_my_user_profile
    }

    override fun getViewModelClass(): Class<MyProfileViewModel> = MyProfileViewModel::class.java

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showPrompt("isFirstEnterMyProfileFragment", context.resources.getString(R.string.close_hint),
                getString(R.string.hint_create_travel), getString(R.string.hint_create_travel_description), addNewTrip)
    }

    private var thisActionIsVkShare: Boolean = false

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!VKSdk.onActivityResult(
                        requestCode,
                        resultCode,
                        data,
                        object : VKCallback<VKAccessToken> {
                            // Пользователь успешно авторизовался
                            override fun onResult(res: VKAccessToken?) {
                                val vkLink = defaultSocialValues[SocialNetwork.VK.link].plus(res?.userId)
                                addNewSocLink(SocialNetwork.VK.link, newLink = vkLink)
                                socialValues[SocialNetwork.VK.link] = vkLink
                                if (thisActionIsVkShare)
                                    shareVKPost()
                                else
                                    openDialogVK(SocialNetwork.VK.link)
                            }

                            override fun onError(error: VKError?) {
                                Log.i("LOG", " Произошла ошибка авторизации  vk (например, пользователь запретил авторизацию" + error?.errorMessage
                                        + " reason=" + error.toString())
                                FirebaseCrash.report(error?.httpError)

                            }
                        }
                )) {
            super.onActivityResult(requestCode, resultCode, data)
            when (requestCode) {
                PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_FROM -> when (resultCode) {
                    AppCompatActivity.RESULT_OK -> {
                        val place = PlaceAutocomplete.getPlace(activity, data)
                        textCityFrom.setText(place.name)
                        textCityFrom.error = null
                        cityFromLatLng = GeoPoint(place.latLng.latitude, place.latLng.longitude)
                        if (cityFromLatLng.hashCode() == cityToLatLng.hashCode()) {
                            textCityFrom.error = "true"
                            Toast.makeText(this@MyProfileFragment.context,
                                    getString(R.string.fill_diff_cities), Toast.LENGTH_LONG).show()
                        } else {
                            cities[FIRST_INDEX_CITY] = place.name.toString()
                            profileStateHashMap[CITY_FROM] = cityFromLatLng.hashCode().toString()
                            getRoutes()
                            setMarkers(FIRST_CITY_POINT)
                            profileChanged()
                        }
                    }
                    else -> {
                        val status = PlaceAutocomplete.getStatus(activity, data)
                        Log.i("LOG", status.statusMessage + " ")
                        if (textCityFrom.text.isEmpty())
                            textCityFrom.setText("")
                    }
                }

                PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_TO -> when (resultCode) {
                    AppCompatActivity.RESULT_OK -> {
                        val place = PlaceAutocomplete.getPlace(activity, data)
                        textCityTo?.setText(place.name)
                        textCityTo?.error = null
                        cityToLatLng = GeoPoint(place.latLng.latitude, place.latLng.longitude)
                        if (cityToLatLng.hashCode() == cityFromLatLng.hashCode()) {
                            textCityTo.error = "true"
                            Toast.makeText(this@MyProfileFragment.context,
                                    getString(R.string.fill_diff_cities), Toast.LENGTH_LONG).show()
                        } else {
                            cities[LAST_INDEX_CITY] = place.name.toString()
                            profileStateHashMap[CITY_TO] = cityToLatLng.hashCode().toString()
                            getRoutes()
                            setMarkers(LAST_CITY_POINT)
                            profileChanged()
                        }
                    }
                    else -> {
                        val status = PlaceAutocomplete.getStatus(activity, data)
                        Log.i("LOG", status.statusMessage + " ")
                        if (textCityTo.text.isEmpty())
                            textCityTo.setText("")
                    }
                }
                PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_FROM_MIDDLE_CITY -> when (resultCode) {
                    AppCompatActivity.RESULT_OK -> {
                        val place = PlaceAutocomplete.getPlace(activity, data)
                        textCityMiddleTwo.setText(place.name)
                        textCityMiddleTwo.error = null
                        cityTwoLatLng = GeoPoint(place.latLng.latitude, place.latLng.longitude)
                        if (cityTwoLatLng.hashCode() == cityFromLatLng.hashCode() || cityTwoLatLng == cityToLatLng) {
                            textCityTo.error = "true"
                            Toast.makeText(this@MyProfileFragment.context,
                                    getString(R.string.fill_diff_cities), Toast.LENGTH_LONG).show()
                        } else {
                            cities[TWO_INDEX_CITY] = place.name.toString()
                            profileStateHashMap[CITY_TWO] = cityTwoLatLng.hashCode().toString()
                            getRoutes()
                            setMarkers(TWO_CITY_POINT)
                            profileChanged()
                        }
                    }
                    else -> {
                        val status = PlaceAutocomplete.getStatus(activity, data)
                        Log.i("LOG", status.statusMessage + " ")
                        if (textCityMiddleTwo.text.isEmpty())
                            textCityMiddleTwo.setText("")
                    }
                }
                PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_TO_NEW_CITY -> when (resultCode) {
                    AppCompatActivity.RESULT_OK -> {
                        val place = PlaceAutocomplete.getPlace(activity, data)
                        textNewCity?.setText(place.name)
                        textNewCity?.error = null
                        cityToLatLng = GeoPoint(place.latLng.latitude, place.latLng.longitude)
                        if (cityToLatLng.hashCode() == cityFromLatLng.hashCode() || cityToLatLng.hashCode() == cityTwoLatLng.hashCode()) {
                            textCityTo.error = "true"
                            Toast.makeText(this@MyProfileFragment.context,
                                    getString(R.string.fill_diff_cities), Toast.LENGTH_LONG).show()
                        } else {
                            cities[LAST_INDEX_CITY] = place.name.toString()
                            profileStateHashMap[CITY_TO] = cityToLatLng.hashCode().toString()
                            getRoutes()
                            setMarkers(LAST_CITY_POINT)
                            profileChanged()
                        }
                    }
                    else -> {
                        val status = PlaceAutocomplete.getStatus(activity, data)
                        Log.i("LOG", status.statusMessage + " ")
                        if (textNewCity.text.isEmpty())
                            textNewCity.setText("")
                    }
                }
                READ_REQUEST_CODE -> when (resultCode) {
                    Activity.RESULT_OK -> {
                        // The document selected by the user won't be returned in the intent.
                        // Instead, a URI to that document will be contained in the return intent
                        // provided to this method as a parameter.
                        // Pull that URI using resultData.getData().
                        var uri: Uri? = null
                        if (data != null) {
                            uri = data.getData()
                            Log.i("LOG", "Uri: ${uri.path} ${uri.encodedPath}" + uri?.toString())
                            viewModel?.loadImage(uri, FirebaseAuth.getInstance().currentUser?.uid!!, mainUser)
                        }
                    }
                }
            }
        }
    }

    /**
     *
     * Если у нас сейчас заполнение только двух городов то надо сделать вызов после заполнения двух городов.
     * Если у нас сейчас заполнение (@MODE_TWO_CITY == false) трех городов то надо также проверить на заполняемость всех трех городов
     *
     */
    private fun getRoutes() {
        if ((cityToLatLng.longitude != 0.0 && cityFromLatLng.longitude != 0.0
                        && cityToLatLng.latitude != 0.0 && cityFromLatLng.latitude != 0.0 && MODE_TWO_CITY)
                ||
                (cityToLatLng.longitude != 0.0 && cityFromLatLng.longitude != 0.0
                        && cityToLatLng.latitude != 0.0 && cityFromLatLng.latitude != 0.0
                        && !MODE_TWO_CITY && cityTwoLatLng.latitude != 0.0 && cityTwoLatLng.longitude != 0.0))
            obtainDirection()
    }

    override fun onResume() {
        super.onResume()
        try {
            mapView?.onResume()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onMapReady(map: GoogleMap?) {
        this.googleMap = map
        viewModel?.load(uid)
    }

    val TWO_CITY_POINT = 3
    val FIRST_CITY_POINT = 1
    val LAST_CITY_POINT = 2

    private fun setMarkers(position: Int) {
        googleMap?.clear()
        val coordFrom = LatLng(cityFromLatLng.latitude, cityFromLatLng.longitude)
        val coordMiddle = LatLng(cityTwoLatLng.latitude, cityTwoLatLng.longitude)
        val coordTo = LatLng(cityToLatLng.latitude, cityToLatLng.longitude)
        var markerTitleStart: String? = ""
        var markerTitleFinal: String? = ""
        var markerTwoTitle: String? = ""
        var markerPositionStart = LatLng(0.0, 0.0)
        var markerPositionTwo = LatLng(0.0, 0.0)
        var markerPositionFinal = LatLng(0.0, 0.0)
        val nameCityFrom = cities[FIRST_INDEX_CITY]
        val nameCityTo = cities[LAST_INDEX_CITY]
        if (position == FIRST_CITY_POINT) {
            markerTitleStart = nameCityTo
            markerTitleFinal = nameCityFrom
            markerPositionStart = coordTo
            markerPositionFinal = coordFrom

        } else if (position == LAST_CITY_POINT) {
            markerTitleFinal = nameCityTo
            markerTitleStart = nameCityFrom
            markerPositionFinal = coordTo
            markerPositionStart = coordFrom
        } else if (position == TWO_CITY_POINT) {
            markerPositionTwo = coordMiddle
            markerTwoTitle = cities[TWO_INDEX_CITY]
        }

        val midPointLat = (coordFrom.latitude + coordTo.latitude) / 2
        val midPointLong = (coordFrom.longitude + coordTo.longitude) / 2
        val blueMarker = BitmapDescriptorFactory.fromResource(R.drawable.pin_blue)
        if (cityTwoLatLng.latitude != 0.0 && cityTwoLatLng.longitude != 0.0) {
            googleMap?.addMarker(MarkerOptions()
                    .icon(blueMarker)
                    .position(LatLng(cityTwoLatLng.latitude, cityTwoLatLng.longitude))
                    .title(cities[Constants.TWO_INDEX_CITY])
                    .anchor(0.5F, 1.0F)
                    .flat(true))
        }
        if (markerPositionStart != LatLng(0.0, 0.0)) {
            googleMap?.addMarker(MarkerOptions()
                    .icon(blueMarker)
                    .position(markerPositionStart)
                    .title(markerTitleStart)
                    .anchor(0.5F, 1.0F)
                    .flat(true))
        }
        if (markerPositionTwo != LatLng(0.0, 0.0)) {
            googleMap?.addMarker(MarkerOptions()
                    .icon(blueMarker)
                    .position(markerPositionTwo)
                    .title(markerTwoTitle)
                    .anchor(0.5F, 1.0F)
                    .flat(true))
        }

        if (markerPositionFinal != LatLng(0.0, 0.0)) {
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(markerPositionFinal, 4.0f))
            googleMap?.addMarker(MarkerOptions()
                    .icon(blueMarker)
                    .position(markerPositionFinal)
                    .title(markerTitleFinal)
                    .anchor(0.5F, 1.0F)
                    .flat(true))

            if (markerPositionStart != LatLng(0.0, 0.0)) {
                val perfectZoom = (190 / coordFrom.getBearing(coordTo)) - 1
                googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(midPointLat, midPointLong), perfectZoom))
            } else
                googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(markerPositionFinal, 2.0f))
        }
    }

    fun drawPolyline() {

        val blueColor = activity.resources.getColor(R.color.blueRouteLine)
        val options = PolylineOptions()
        options.color(blueColor)

        options.width(5f)

        if (routeString != "") {
            options.addAll(PolyUtil.decode(routeString))
        } else {
            // fix me рисуем прямую линию ( как будто на самолете летим)
            options.add(cityFromLatLng.toLatLng())
            if (!MODE_TWO_CITY) options.add(cityTwoLatLng.toLatLng())
            options.add(cityToLatLng.toLatLng())
        }
        options.geodesic(true)
        googleMap?.addPolyline(options)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mapView = view?.findViewById(R.id.mapView)
        try {
            mapView?.onCreate(null)
            MapsInitializer.initialize(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mapView?.getMapAsync(this@MyProfileFragment)
        val scroll = view?.findViewById(R.id.scrollProfile) as ScrollView
        scroll.descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS
        scroll.isFocusable = true
        scroll.isFocusableInTouchMode = true
        return view
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
        super.onDetach()
        mListener = null
    }

    private
    var MODE_TWO_CITY: Boolean = true

    override fun onStart() {
        super.onStart()

        displayPriceTravel.text = StringBuilder(getString(R.string.type_money)).append(budget)

        val now = Calendar.getInstance()

        dateDialog = CalendarDatePickerDialogFragment()
                .setFirstDayOfWeek(Calendar.MONDAY)
                .setThemeCustom(R.style.BythewayDatePickerDialogTheme)
                .setPreselectedDate(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH))

        headerprofile.setOnClickListener {
            openInformationEditDialog()
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_header_click", null)
        }

        add_city.setOnClickListener {
            MODE_TWO_CITY = !MODE_TWO_CITY
            Log.e("LOG", "mode:" + MODE_TWO_CITY)
            if (MODE_TWO_CITY == true) {
                // DELETE CITY
                clearLastCity()
            } else {
                // ADD NEW CITY
                addNewCity()
            }
        }

        choosePriceTravel.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, number: Int, p2: Boolean) {
                budget = (START_BUDGET * number).toLong()
                displayPriceTravel.text = budget.toString()
                if (number != budgetPosition) {
                    budgetPosition = number
                    profileStateHashMap[BUDGET] = budget.toString()
                    profileStateHashMap[BUDGET_POSITION] = number.toString()
                    profileChanged()
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
        for ((key, me) in methodIcons) {
            me.setOnClickListener {
                val textView = methodTextViews[key]
                textView?.let {
                    me.isActivated = !it.isActivated
                    methods.put(key, it.isActivated)
                }
                profileStateHashMap[METHODS] = methods.hashCode().toString()
                profileChanged()
            }
        }

        newTripText.setOnClickListener {
            hideBlockNewTrip()
            showBlockTravelInformation()
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_create_click", null)
        }
        vkIcon.setOnClickListener {
            /*
             Если контакты еще не добавлены, тогда открываем диалоговое окно.
             Если были какие-то изменения в линках то сохраняем в бд.
             И меняем цвет иконки соответсвенно значениям.
             */
            openDialogVK(SocialNetwork.VK.link)
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_vk_click", null)
        }
        csIcon.setOnClickListener {
            openDialog(SocialNetwork.CS)
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_cs_click", null)
        }
        whatsAppIcon.setOnClickListener {
            openDialogWhatsApp(SocialNetwork.WHATSAPP.link)
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_wap_click", null)
        }
        fbcon.setOnClickListener {
            openDialog(SocialNetwork.FB)
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_fb_click", null)
        }
        tgIcon.setOnClickListener {
            openDialogTelegram(SocialNetwork.TG.link)
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_tg_click", null)
        }
        buttonSaveTravelInfo.setOnClickListener {
            if (checkingCityText())
                sendUserInfoToServer()
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_save", null)
        }
        textDateArrived.setOnClickListener {
            openDateDialog(END_DATE, textDateArrived)
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_date_to_click", null)
        }
        textDateArrived.setOnTouchListener(DateUtils.onDateTouch)

        textDateFrom.setOnClickListener {
            openDateDialog(START_DATE, textDateFrom)
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_date_from_click", null)
        }
        textDateFrom.setOnTouchListener(DateUtils.onDateTouch)

        addInfoUser.afterTextChanged({
            profileStateHashMap[ADD_INFO] = it
            profileChanged(null, false)
        })
        textCityFrom.setOnClickListener {
            sendIntentForSearch(PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_FROM)
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_city_from_change", null)
        }
        textCityTo.setOnClickListener {
            sendIntentForSearch(PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_TO)
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_city_to_change", null)
        }
        appinTheAirEnter.setOnClickListener {
            (activity as MenuActivity).navigator
                    .applyCommand(Replace(Screens.USER_SINHRONIZED_SCREEN, 1))
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_click_air", null)
        }
        vkApp.setOnClickListener {
            shareVKPost()
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_click_vk_share", null)
        }
        buttonRemoveTravelInfo.setOnClickListener {
            openAlertDialog(this::removeTrip)
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_remove_trip", null)
        }
    }

    private fun shareVKPost() {
        thisActionIsVkShare = true
        val s = VK()
        val cityArrived = textCityFrom.text.toString()
        val cityTo = getLastCity()
        val text = "Ищу попутчика в $cityTo \nhttps://www.google.ru/maps/dir/$cityArrived/$cityTo \n\n\n ${getHashTags(addInfoUser.text.toString())}"
        scrollProfile.scrollTo(0, appinTheAirEnter.top)
        val totalHeight = scrollProfile.getChildAt(0).height
        val pos = totalHeight - appinTheAirEnter.top
        scrollProfile.scrollBy(0, -pos)

        if (!s.start(activity))
            s.postToWall(activity, text, screenShot(view ?: return))
    }

    private fun getHashTags(text: String): String {
        val hashTagText = StringBuilder("#bytheway ")
        hashTagText.append("#поискпопутчиков ")
        hashTagText.append("#ищупопутчика")
        hashTagText.append("#").append(textCityFrom.text.toString()).append(" ")
        hashTagText.append("#").append(getLastCity()).append("2018 ")
        if (mainUser?.method!![Method.HITCHHIKING.link] == true) {
            hashTagText.append("#автостоп ")
            hashTagText.append("#HITCHHIKING2018 ")
        }
        if (mainUser?.method!![Method.TRAIN.link] == true) {
            hashTagText.append("#напоезде ")
        }
        if (mainUser?.method!![Method.PLANE.link] == true) {
            hashTagText.append("#самолетом ")
        }
        //hashTagText.append("#")
//        text.split(',', ' ', '!', '?', '.', '+').forEachIndexed { index, s ->
//            if (index < 2) hashTagText.append(s) // первые два слова
//            if (s.contains("([A-Z]+[a-z][A-Z]*)|([A-Z]*[a-z][A-Z]+)")) hashTagText.append("#").append(s).append("2018")
//        }
        return hashTagText.toString()
    }

    private fun getLastCity(): String =
            if (MODE_TWO_CITY)
                textCityTo.text.toString()
            else
                textNewCity.text.toString()


    fun screenShot(view: View): Bitmap {
        view.isDrawingCacheEnabled = true
        view.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_AUTO
        val screenshot = Bitmap.createBitmap(view.drawingCache)
        view.isDrawingCacheEnabled = false;
        return screenshot;
    }

    private fun addNewCity() {
        mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_add_new_city", null)
        show3CitiesBlock()
        //clear old data for new field
        cityTwoLatLng = cityToLatLng
        cityToLatLng = GeoPoint(0.0, 0.0)
        cities[LAST_INDEX_CITY]?.let {
            cities[TWO_INDEX_CITY] = it
        }
        dates[TWO_DATE] = dates[END_DATE] ?: 0
    }

    private fun clearLastCity() {
        mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_remove_new_city", null)

        add_city.text = getString(R.string.add_city)
        new_cities_block.visibility = View.GONE
        textCityTo.visibility = View.VISIBLE
        textDateFrom.visibility = View.VISIBLE
        line2.visibility = View.INVISIBLE
        line3.visibility = View.INVISIBLE
        line1.visibility = View.VISIBLE
        line1_one.visibility = View.GONE
        textNewCity.setText("")
        dateFinish.setText("")

        textCityTo.text = textCityMiddleTwo.text
        textDateFrom.text = dateStartTwo.text
        textDateFrom.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_close_vector, 0)

        textCityMiddleTwo.visibility = View.GONE
        dateStartTwo.visibility = View.GONE

        cities[TWO_INDEX_CITY]?.let {
            cities[LAST_INDEX_CITY] = it
        }
        cities.remove(TWO_INDEX_CITY)
        dates[END_DATE] = dates[TWO_DATE] ?: 0
        dates.remove(TWO_DATE)
        cityToLatLng = cityTwoLatLng
        cityTwoLatLng = GeoPoint(0.0, 0.0)
        setMarkers(LAST_CITY_POINT)
        getRoutes()
    }

    private fun show3CitiesBlock() {
        add_city.text = getString(R.string.remove_city)
        new_cities_block.visibility = View.VISIBLE
        textDateFrom.visibility = View.GONE
        textCityTo.visibility = View.GONE
        dateStartTwo.visibility = View.VISIBLE
        line2.visibility = View.VISIBLE
        line3.visibility = View.VISIBLE
        line1.visibility = View.GONE
        line1_one.visibility = View.VISIBLE
        textCityMiddleTwo.visibility = View.VISIBLE
        textCityMiddleTwo.text = textCityTo.text
        dateStartTwo.text = textDateFrom.text
        dateStartTwo.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_close_vector, 0)
        dateFinish.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_close_vector, 0)

        dateStartTwo.setOnClickListener {
            openDateDialog(START_DATE, dateStartTwo)
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_date_from_click", null)
        }
        dateStartTwo.setOnTouchListener(DateUtils.onDateTouch)

        dateFinish.setOnClickListener {
            openDateDialog(END_DATE, dateFinish)
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_date_to_click", null)
        }
        dateFinish.setOnTouchListener(DateUtils.onDateTouch)

        textDateArrived.setOnClickListener {
            openDateDialog(TWO_DATE, textDateArrived)
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_date_midle_click", null)
        }
        textDateArrived.setOnTouchListener(DateUtils.onDateTouch)

        textCityFrom.setOnClickListener {
            sendIntentForSearch(PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_FROM)
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_city_from_change", null)
        }

        textNewCity.setOnClickListener {
            sendIntentForSearch(PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_TO_NEW_CITY)
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_city_to_change", null)
        }

        textCityMiddleTwo.setOnClickListener {
            sendIntentForSearch(PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_FROM_MIDDLE_CITY)
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_city_ml_change", null)
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            mapView?.onStop()
        } catch (e: Exception) {
            e.printStackTrace()
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
            FirebaseCrash.report(e)
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    private fun TextView.afterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(editable: Editable?) {
                afterTextChanged.invoke(editable.toString())
            }
        })
    }

    private fun sendIntentForSearch(code: Int) {
        try {
            val typeFilter = AutocompleteFilter.Builder()
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_REGIONS)
                    .build()
            val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).setFilter(typeFilter).build(activity)

            startActivityForResult(intent, code)
        } catch (e: GooglePlayServicesRepairableException) {
            e.printStackTrace()
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        }
    }

    private fun openDateDialog(key: String, view: EditText) {
        if (view.text.contains("  ")) {
            view.setText("")
            dates[key] = 0L
            view.setCompoundDrawables(null, null, null, null)
            return
        }
        val date = Calendar.getInstance() //current time by default
        if (dates[key] ?: 0L > 0L)
            date.timeInMillis = dates[key] ?: date.timeInMillis

        dateDialog = CalendarDatePickerDialogFragment()
                .setFirstDayOfWeek(Calendar.MONDAY)
                .setThemeCustom(R.style.BythewayDatePickerDialogTheme)
                .setPreselectedDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH))
        val currentDate = System.currentTimeMillis()

        dateDialog.setDateRange(MonthAdapter.CalendarDay(currentDate), null)
        dateDialog.setOnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            view.setText(StringBuilder(" ")
                    .append(dayOfMonth)
                    .append(" ")
                    .append(context.resources.getStringArray(R.array.months_array)[monthOfYear])
                    .append(" ")
                    .append(year).toString())
            dates[key] = DateUtils.getLongFromDate(dayOfMonth, monthOfYear, year)

            profileStateHashMap[DATES] = dates.toString()
            profileChanged()
            view.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_close_vector, 0)
        }
        dateDialog.show(activity.supportFragmentManager, "")
    }

    private fun sendUserInfoToServer() {
        countTrip = 1

        viewModel?.sendUserData(getHashMapUser(), uid, mainUser)

        if (socNet.size == 0)
            showTipsForEmptySocialLink()
    }

    private fun checkingCityText(): Boolean {
        var errorString = ""


        if (textCityFrom.text.isEmpty()) {
            textCityFrom.error = getString(R.string.name)
            errorString = getString(R.string.fill_required_fields) + " " + getString(R.string.city_from)
        } else
            textCityFrom.error = null

        if (textCityTo.text.isEmpty() && MODE_TWO_CITY) {
            textCityTo.error = getString(R.string.yes)
            errorString += getString(R.string.fill_required_fields) + " " + getString(R.string.city_to)
        } else
            textCityTo.error = null

        if (textNewCity.text.isEmpty() && !MODE_TWO_CITY) {
            textNewCity.error = getString(R.string.yes)
            errorString += getString(R.string.fill_required_fields) + " " + getString(R.string.city_to)
        } else
            textNewCity.error = null

        if (textCityMiddleTwo.text.isEmpty() && !MODE_TWO_CITY) {
            textCityMiddleTwo.error = getString(R.string.yes)
            errorString += getString(R.string.fill_required_fields) + " " + getString(R.string.city_to)
        } else
            textCityMiddleTwo.error = null

        textCityFrom.parent.requestChildFocus(textCityFrom, textCityFrom)

        if (isDifferentLocationCity()) {
            errorString += " " + getString(R.string.fill_diff_cities)
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_city_equals_err", null)
        }

        if (errorString.isNotBlank()) {
            Toast.makeText(this@MyProfileFragment.context, errorString, Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun isDifferentLocationCity(): Boolean {
        return ((cityToLatLng.hashCode() == cityFromLatLng.hashCode())
                || (cityToLatLng.hashCode() == cityTwoLatLng.hashCode())
                || (cityTwoLatLng.hashCode() == cityFromLatLng.hashCode()))
    }

    private fun showErrorResponse() {
        Toast.makeText(this@MyProfileFragment.context,
                getString(R.string.error_update), Toast.LENGTH_SHORT).show()
        mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_error_update", null)
        mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_error_update", null)
    }

    private fun showBlockTravelInformation() {
        direction.visibility = View.VISIBLE
        maplayout.visibility = View.VISIBLE
        methodMoving.visibility = View.VISIBLE
        layoutTravelMethod.visibility = View.VISIBLE
        moneyfortrip.visibility = View.VISIBLE
        appinTheAirEnter.visibility = View.VISIBLE
        vkApp.visibility = if (!vkAppIsInstalled()) View.GONE else View.VISIBLE
        descriptionprofile.visibility = View.VISIBLE
        buttonRemoveTravelInfo.visibility = View.VISIBLE
        buttonSaveTravelInfo.visibility = View.VISIBLE
    }

    private fun hideBlockNewTrip() {
        addNewTrip.visibility = View.GONE
        grayLine.visibility = View.VISIBLE
    }

    private fun openInformationEditDialog() {
        val simpleAlert = AlertDialog.Builder(activity).create()
        val dialogView = View.inflate(context, R.layout.custom_dialog_profile_inforamtion, null)

        simpleAlert.setView(dialogView)
        val nameChoose = dialogView.findViewById<View>(R.id.dialog_name) as EditText
        nameChoose.setText(name)
        val lastNameChoose = dialogView.findViewById<View>(R.id.dialog_last_name) as EditText
        lastNameChoose.setText(lastName)
        val cityChoose = dialogView.findViewById<View>(R.id.dialog_city) as EditText
        cityChoose.setText(city)

        val man = dialogView.findViewById<RadioButton>(R.id.man)
        val woman = dialogView.findViewById<RadioButton>(R.id.woman)

        val sexChoose = dialogView.findViewById<RadioGroup>(R.id.sex)
        if (sex == 1) {
            sexChoose.check(man.id)
        } else if (sex == 2) {
            sexChoose.check(woman.id)
        }

        val yearsView = dialogView.findViewById<EditText>(R.id.yearsView)
        yearsView.setText(age.toStringOrBlank())
        yearsView.filters = arrayOf(DecimalInputFilter())

        simpleAlert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.save), { _, _ ->
            sex = if (man.isChecked) 1 else if (woman.isChecked) 2 else 0
            name = (nameChoose.text.toString()).capitalize()
            lastName = (lastNameChoose.text.toString()).capitalize()
            city = (cityChoose.text.toString()).capitalize()
            age = (yearsView.text.toStringOrZero()).toInt()

            username.text = StringBuilder(name).append(" ").append(lastName)
            cityview.text = if (city.isNotEmpty()) city else getString(R.string.native_city)
            fillAgeSex(age, sex)

            savingUserData(name, lastName, city, age, sex)
        })
        simpleAlert.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel), { _, _ ->
            simpleAlert.hide()
        })

        var enterCounter = 0
        nameChoose.setOnKeyListener(View.OnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                enterCounter = 1
                lastNameChoose.requestFocus()
                return@OnKeyListener true
            }
            false
        })

        lastNameChoose.setOnKeyListener(View.OnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (enterCounter == 0) {
                    cityChoose.requestFocus()
                    enterCounter = 1
                } else enterCounter = 0
                return@OnKeyListener true
            }
            false
        })

        cityChoose.setOnKeyListener(View.OnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (enterCounter == 0) {
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(cityChoose.windowToken, 0)
                } else enterCounter = 0
                return@OnKeyListener true
            }
            false
        })
        simpleAlert.show()
    }

    private fun savingUserData(name: String, lastName: String, city: String, age: Int, sex: Int) {

        val hashMap = hashMapOf(
                NAME to name,
                LASTNAME to lastName,
                CITY to city,
                AGE to age,
                SEX to sex
        )

        viewModel?.sendUserData(hashMap, uid, mainUser)
    }

    private fun openDialogWhatsApp(socialNetworkLink: String) {
        val dialog = AlertDialog.Builder(activity).create()
        if (dialog.isShowing) return

        dialog.setTitle(getString(R.string.social_title_wp))
        dialog.setMessage(getString(R.string.social_text_wp))
        val dialogView = View.inflate(context, R.layout.custom_dialog_profile_add_wp, null)
        dialog.setView(dialogView)
        dialogView.findViewById<EditText>(R.id.socLinkText).setText(mainUser?.phone)
        socialValues[socialNetworkLink]?.let {
            dialogView.findViewById<EditText>(R.id.socLinkText).setText(it)
        }
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.remove), { _, _ ->
            removeSocNet(socialNetworkLink)
        })

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.save), { _, _ ->
            val newLink = dialogView.findViewById<EditText>(R.id.socLinkText).text.toString()
            if (!newLink.isNumberPhone()) {
                dialogView.findViewById<EditText>(R.id.socLinkText).error = getString(R.string.fill_phone_invalid)
            } else {
                if (newLink in defaultSocialValues.values) return@setButton
                addNewSocLink(socialNetworkLink, newLink)
            }
        })

        dialogView.findViewById<EditText>(R.id.socLinkText).afterTextChanged {
            val valid = it.isNumberPhone()
            if (!valid) {
                dialogView.findViewById<EditText>(R.id.socLinkText).error = getString(R.string.fill_phone_invalid)
            }
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = valid
        }
        dialog.show()
    }

    private fun openDialogTelegram(socialNetworkLinkKey: String) {
        val dialog = AlertDialog.Builder(activity).create()
        if (dialog.isShowing) return
        dialog.setTitle(getString(R.string.social_title_tg))
        dialog.setMessage(getString(R.string.social_text_tg))
        val dialogView = View.inflate(context, R.layout.custom_dialog_profile_add_tg, null)
        dialog.setView(dialogView)

        socialValues[socialNetworkLinkKey]?.let {
            dialogView.findViewById<EditText>(R.id.socLinkText).setText(it)
        }

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.remove), { _, _ ->
            removeSocNet(socialNetworkLinkKey)
        })

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.save), { _, _ ->
            val newLink = dialogView.findViewById<EditText>(R.id.socLinkText).text.toString()
            if (newLink in defaultSocialValues.values) return@setButton
            addNewSocLink(socialNetworkLinkKey, newLink)

        })

        dialogView.findViewById<EditText>(R.id.socLinkText).afterTextChanged {
            val valid = it.isNumberPhone() || it.startsWith("@")
            if (!valid) {
                dialogView.findViewById<EditText>(R.id.socLinkText).error = getString(R.string.fill_phone_invalid)
            }
        }
        dialog.show()
    }

    private fun openDialogVK(socialNetworkLinkKey: String) {
        val dialog = AlertDialog.Builder(activity).create()
        if (dialog.isShowing) return
        dialog.setTitle(getString(R.string.social_title_vk))
        dialog.setMessage(getString(R.string.social_text_vk))
        val dialogView = View.inflate(context, R.layout.custom_dialog_profile_add_vk, null)
        dialog.setView(dialogView)


        if (shouldVkButtonShow(socialNetworkLinkKey))
            dialogView.findViewById<Button>(R.id.vkAppSinch).visibility = View.GONE


        dialogView.findViewById<Button>(R.id.vkAppSinch).setOnClickListener {
            sinchVk()
            dialog.cancel()
        }

        socialValues[socialNetworkLinkKey]?.let {
            dialogView.findViewById<EditText>(R.id.socLinkText).setText(it)
        }

        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.remove), { _, _ ->
            removeSocNet(socialNetworkLinkKey)
        })

        dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.save), { _, _ ->
            val newLink = dialogView.findViewById<EditText>(R.id.socLinkText)?.text.toString()
            if (newLink in defaultSocialValues.values) return@setButton
            addNewSocLink(socialNetworkLinkKey, newLink)
        })
        dialog.show()
    }

    private fun shouldVkButtonShow(socialNetworkLinkKey: String) =
            !vkAppIsInstalled() || VKSdk.isLoggedIn() || isLinkNotDefault(socialNetworkLinkKey)


    private fun vkAppIsInstalled(): Boolean {
        val pm = activity.packageManager
        try {
            pm.getPackageInfo("com.vkontakte.android", PackageManager.GET_ACTIVITIES);
            return true;
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return false;
    }

    private fun isLinkNotDefault(key: String) = !(socialValues[key] in defaultSocialValues.values)

    private fun sinchVk() {
        val s = VK()
        if (!s.start(activity)) {
            Toast.makeText(activity, "Already", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openDialog(socialNetwork: SocialNetwork) {
        val simpleAlert = AlertDialog.Builder(activity).create()
        simpleAlert.setTitle(getString(R.string.social_links))
        simpleAlert.setMessage(getString(R.string.social_text))
        val dialogView = View.inflate(context, R.layout.custom_dialog_profile_soc_network, null)

        simpleAlert.setView(dialogView)
        var socValueDone = ""
        socialValues[socialNetwork.link]?.let {
            socValueDone = it
        }
        if (socialNetwork == SocialNetwork.TG && socValueDone.length <= 1) {
            socValueDone = numberPhone
        }
        dialogView.findViewById<EditText>(R.id.socLinkText).setText(socValueDone)

        simpleAlert.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.remove), { _, _ ->
            removeSocNet(socialNetwork.link)
        })

        simpleAlert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.save), { _, _ ->
            val newLink = dialogView.findViewById<EditText>(R.id.socLinkText).text.toString()
            if (newLink in defaultSocialValues.values) return@setButton
            addNewSocLink(socialNetwork.link, newLink)
        })

        simpleAlert.show()
    }

    private fun addNewSocLink(socialNetworkLink: String, newLink: String) {
        socNet[socialNetworkLink] = newLink
        viewModel?.saveLinks(socNet, uid, SocialResponse(socialNetworkLink, newLink))
        mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_save_links", null)
    }

    private fun removeSocNet(socialNetworkLink: String) {
        mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_remove_links", null)
        socNet.remove(socialNetworkLink)
        viewModel?.saveLinks(socNet, uid, SocialResponse(socialNetworkLink))
    }

    private fun openAlertDialog(callback: () -> Unit) {
        val simpleAlert = AlertDialog.Builder(activity).create()
        val dialogView = View.inflate(context, R.layout.confirm_dialog, null)

        simpleAlert.setView(dialogView)
        dialogView.textMessage.text = getString(R.string.text_confirm_remove_trip)
        simpleAlert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.yes), { _, _ ->
            callback()
        })
        simpleAlert.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.no), { _, _ -> })
        simpleAlert.show()
    }

    private fun changeSocIconsActive(socialNetworkLink: String, newLink: String) {
        socialValues[socialNetworkLink] = newLink
        when (socialNetworkLink) {
            SocialNetwork.VK.link -> vkIcon.setImageResource(R.drawable.ic_vk_color)
            SocialNetwork.CS.link -> csIcon.setImageResource(R.drawable.ic_cs_color)
            SocialNetwork.FB.link -> fbcon.setImageResource(R.drawable.ic_fb_color)
            SocialNetwork.WHATSAPP.link -> whatsAppIcon.setImageResource(R.drawable.ic_whats_icon_color)
            SocialNetwork.TG.link -> tgIcon.setImageResource(R.drawable.ic_tg_color)
        }
    }

    private fun changeSocIconsDisActive(socialNetworkLink: String = "") {
        socNet.remove(socialNetworkLink)
        defaultSocialValues[socialNetworkLink]?.let {
            socialValues[socialNetworkLink] = it
        }
        when (socialNetworkLink) {
            SocialNetwork.VK.link -> vkIcon.setImageResource(R.drawable.ic_vk_gray)
            SocialNetwork.CS.link -> csIcon.setImageResource(R.drawable.ic_cs_grey)
            SocialNetwork.FB.link -> fbcon.setImageResource(R.drawable.ic_fb_grey)
            SocialNetwork.WHATSAPP.link -> whatsAppIcon.setImageResource(R.drawable.ic_whats_icon_grey)
            SocialNetwork.TG.link -> tgIcon.setImageResource(R.drawable.tg_grey)
        }
    }

    private fun hideBlockTravelInforamtion() {
        direction.visibility = View.GONE
        textCityFrom.setText("")
        textCityTo.setText("")
        textDateArrived.setText("")
        textDateFrom.setText("")
        addInfoUser.setText("")
        maplayout.visibility = View.GONE
        methodMoving.visibility = View.GONE

        methodTextViews.values.map {
            it?.isActivated = false
        }
        methodIcons.values.map {
            it?.isActivated = false
        }
        appinTheAirEnter.visibility = View.GONE
        vkApp.visibility = View.GONE
        layoutTravelMethod.visibility = View.GONE
        moneyfortrip.visibility = View.GONE
        displayPriceTravel.text = ""
        descriptionprofile.visibility = View.GONE
        buttonRemoveTravelInfo.visibility = View.GONE
        buttonSaveTravelInfo.visibility = View.GONE
    }

    private fun showBlockAddTrip() {
        addNewTrip.visibility = View.VISIBLE
        grayLine.visibility = View.INVISIBLE
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
            sex = userSex
            if (userAge > 0) {
                sexAndAge.text = StringBuilder(gender).append(", ").append(userAge)
            } else {
                sexAndAge.text = StringBuilder(gender).append(", " +
                        getString(R.string.age) + " ").append(userAge)
            }
        }

        if (userAge > 0) {
            age = userAge
            if (userSex != 0) {
                sexAndAge.text = StringBuilder(gender).append(", ").append(userAge)
            } else {
                sexAndAge.text = StringBuilder(getString(R.string.sex) + ", ").append(userAge)
            }
        }
    }

    private
    val READ_REQUEST_CODE = 42

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    private fun performFileSearch() {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.type = "image/*"

        startActivityForResult(intent, READ_REQUEST_CODE)
    }

    private fun updateImageProfile(link: String) {
        if (link.isNotBlank())
            glide?.load(link)
                    ?.apply(RequestOptions.circleCropTransform())
                    ?.into(image_avatar)
    }

    private fun fillProfile(user: User) {
        updateImageProfile(user.urlPhoto)
        image_avatar.setOnClickListener {
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_click_avatar", null)
            performFileSearch()
        }

        cityFromLatLng = user.cityFromLatLng
        cityToLatLng = user.cityToLatLng
        cityTwoLatLng = user.cityTwoLatLng

        profileStateHashMap.clear()
        lastName = user.lastName
        name = user.name
        numberPhone = user.phone

        travelledStatistics.visibility = if (user.flightHours == 0L) View.GONE else View.VISIBLE

        travelledCountries.text = user.countries.toString()
        flightHours.text = user.flightHours.toString()
        flightDistance.text = user.kilometers.toString()

        if (user.city.isNotEmpty()) {
            cityview.text = user.city
            city = user.city
        }

        countTrip = user.countTrip

        if (countTrip == 0) {
            showBlockAddTrip()
            hideBlockTravelInforamtion()
        } else {
            hideBlockNewTrip()
            showBlockTravelInformation()
        }

        if (user.cities.size == 1 || user.cities.size == 2) {
            textCityFrom.setText(user.cities[FIRST_INDEX_CITY])
            textCityTo.setText(user.cities[LAST_INDEX_CITY])
            cities = user.cities
        } else if (user.cities.size == 3) {
            MODE_TWO_CITY = false // Включаем режим трех городов.
            show3CitiesBlock()
            textCityFrom.setText(user.cities[FIRST_INDEX_CITY])
            textCityMiddleTwo.setText(user.cities[TWO_INDEX_CITY])
            textNewCity.setText(user.cities[LAST_INDEX_CITY])
            cities = user.cities

        }

        val formatDate = SimpleDateFormat("dd.MM.yyyy", Locale.US)

        if (user.dates.size > 0) {
            if (user.cities.size == 2) {
                if (user.dates[START_DATE] != null && user.dates[START_DATE] != 0L) {
                    textDateFrom.setText(formatDate.format(Date(user.dates[START_DATE]
                            ?: 0)))
                    textDateFrom.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_close_vector, 0)
                }
                if (user.dates[END_DATE] != null && user.dates[END_DATE] != 0L) {
                    textDateArrived.setText(formatDate.format(Date(user.dates[END_DATE]
                            ?: 0)))
                    textDateArrived.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_close_vector, 0)
                }
            } else {
                // Для случая когда у нас три города
                if (user.dates[START_DATE] != null && user.dates[START_DATE] != 0L) {
                    dateStartTwo.setText(formatDate.format(Date(user.dates[START_DATE]
                            ?: 0)))
                    dateStartTwo.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_close_vector, 0)
                }
                if (user.dates[END_DATE] != null && user.dates[END_DATE] != 0L) {
                    dateFinish.setText(formatDate.format(Date(user.dates[END_DATE]
                            ?: 0)))
                    dateFinish.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_close_vector, 0)
                }
                if (user.dates[TWO_DATE] != null && user.dates[TWO_DATE] != 0L) {
                    textDateArrived.setText(formatDate.format(Date(user.dates[TWO_DATE]
                            ?: 0)))
                    textDateArrived.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_close_vector, 0)
                }
            }
            dates = user.dates
        }

        fillAgeSex(user.age, user.sex)
        setMarkers(LAST_CITY_POINT)

        if (user.route.isNotBlank()) {
            routeString = user.route
            drawPolyline()
        } else
            obtainDirection() // для страховки. Если машрута нет, то пытаемя получить его.

        age = user.age



        for (name in user.socialNetwork) {
            socNet[name.key] = name.value
            when (name.key) {
                SocialNetwork.VK.link -> vkIcon.setImageResource(R.drawable.ic_vk_color)
                SocialNetwork.CS.link -> csIcon.setImageResource(R.drawable.ic_cs_color)
                SocialNetwork.FB.link -> fbcon.setImageResource(R.drawable.ic_fb_color)
                SocialNetwork.WHATSAPP.link -> whatsAppIcon.setImageResource(R.drawable.ic_whats_icon_color)
                SocialNetwork.TG.link -> tgIcon.setImageResource(R.drawable.ic_tg_color)
            }

            if (name.key in socialValues)
                socialValues[name.key] = name.value

        }
        if (user.token.isEmpty() || (activity as MenuActivity).needUpdateToken()) {
            viewModel?.updateFcmToken()
        }
        if (user.socialNetwork.size == 0 && user.countTrip > 0) {
            showTipsForEmptySocialLink()
        }

        methods.putAll(user.method)
        profileStateHashMap[METHODS] = methods.hashCode().toString()

        user.method.keys.map {
            methodIcons[it]?.isActivated = user.method[it] == true
        }
        if (user.budget > 0) {
            budget = user.budget
            displayPriceTravel.text = StringBuilder(getString(R.string.type_money)).append(budget)
            budgetPosition = user.budgetPosition
            choosePriceTravel.progress = budgetPosition
        }

        profileStateHashMap[ADD_INFO] = user.addInformation
        saveProfileState()
        addInfoUser.setText(user.addInformation)
        addInfoUser.clearFocus()

        val navigationView = activity.findViewById<NavigationView>(R.id.nav_view)
        val hView = navigationView.getHeaderView(0)
        val cityName = hView.findViewById<TextView>(R.id.menu_city_name)
        cityName.text = user.city
        val fullName = hView.findViewById<TextView>(R.id.menu_fullname)
        fullName.text = StringBuilder().append(user.name).append(" ").append(user.lastName)
        username.text = StringBuilder(user.name).append(" ").append(user.lastName)
    }

    private fun showTipsForEmptySocialLink() {
        val tips = SocialTipsDialog()
        tips.show(fragmentManager, "Tips")
        scrollProfile?.fullScroll(ScrollView.FOCUS_UP)
    }


    private fun removeTrip() {
        countTrip = 0
        budget = 0
        methods.clear()
        cities.clear()
        cityFromLatLng = GeoPoint(0.0, 0.0)
        cityToLatLng = GeoPoint(0.0, 0.0)
        cityTwoLatLng = GeoPoint(0.0, 0.0)
        dates.clear()
        googleMap?.clear()
        routeString = ""
        viewModel?.sendUserData(getHashMapUser(), uid, mainUser)
        hideBlockTravelInforamtion()
        showBlockAddTrip()
        textDateArrived.setCompoundDrawables(null, null, null, null)
        textDateFrom.setCompoundDrawables(null, null, null, null)
    }

    private fun saveProfileState() {
        profileStateHashMap[DATES] = dates.toString()
        profileStateHashMap[BUDGET] = budget.toString()
        profileStateHashMap[BUDGET_POSITION] = budgetPosition.toString()
        profileStateHashMap[CITY_FROM] = cityFromLatLng.hashCode().toString()
        profileStateHashMap[CITY_TO] = cityToLatLng.hashCode().toString()
        oldProfileState = profileStateHashMap.hashCode()
    }

    fun getHashMapUser(): HashMap<String, Any> {
        if (budget > 0)
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_budget_more_0", null)
        if (addInfoUser.text.toString().isNotBlank())
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_add_info", null)

        val hashMap = HashMap<String, Any>()

        hashMap[CITIES] = cities
        hashMap[METHOD] = methods
        hashMap[DATES] = dates
        hashMap[ROUTE] = routeString
        hashMap[BUDGET] = budget
        hashMap[BUDGET_POSITION] = budgetPosition
        hashMap[CITY_FROM] = cityFromLatLng
        hashMap[CITY_TWO] = cityTwoLatLng
        hashMap[CITY_TO] = cityToLatLng
        hashMap[ADD_INFO] = addInfoUser.text.toString()
        hashMap[COUNT_TRIP] = countTrip
        hashMap[SEX] = sex
        hashMap[AGE] = age
        hashMap[NAME] = name
        hashMap[LASTNAME] = lastName
        hashMap[CITY] = city
        return hashMap
    }

    fun profileChanged(force: Boolean? = null, removeFocus: Boolean? = true) {
        if (activity == null) return
        if (removeFocus == true) {
            addInfoUser.clearFocus()
        }
        if (countTrip == 1) {
            var changed: Boolean = profileStateHashMap.hashCode() != oldProfileState
            force?.let { changed = it }
            (activity as MenuActivity).profileChanged = changed
        }
    }

    private fun obtainDirection() {
        viewModel?.getRoute(cityFromLatLng = cityFromLatLng, cityToLatLng = cityToLatLng, waypoint = cityTwoLatLng)
    }
}

private fun GeoPoint.toLatLng(): LatLng? = LatLng(this.latitude, this.longitude)

private fun Int.toStringOrBlank(): String = if (this == 0) "" else this.toString()

private fun Editable.toStringOrZero(): String = if (this.toString().isBlank()) "0" else this.toString()
