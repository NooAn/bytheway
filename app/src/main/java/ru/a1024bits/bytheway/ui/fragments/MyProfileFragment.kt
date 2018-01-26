package ru.a1024bits.bytheway.ui.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.annotation.LayoutRes
import android.support.design.widget.NavigationView
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crash.FirebaseCrash
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.PolyUtil
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
import ru.a1024bits.bytheway.util.Constants.END_DATE
import ru.a1024bits.bytheway.util.Constants.FIRST_INDEX_CITY
import ru.a1024bits.bytheway.util.Constants.LAST_INDEX_CITY
import ru.a1024bits.bytheway.util.Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_FROM
import ru.a1024bits.bytheway.util.Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_TO
import ru.a1024bits.bytheway.util.Constants.START_DATE
import ru.a1024bits.bytheway.util.DecimalInputFilter
import ru.a1024bits.bytheway.util.getBearing
import ru.a1024bits.bytheway.util.getLongFromDate
import ru.a1024bits.bytheway.viewmodel.MyProfileViewModel
import ru.terrakok.cicerone.commands.Replace
import uk.co.deanwild.materialshowcaseview.IShowcaseListener
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
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
    private val usersObservers: Observer<User> = Observer { user ->
        mListener?.onFragmentInteraction(user)
    }


    private val responseObserver: Observer<ResponseBtw<User>> = Observer { response ->
        when (response?.status) {
            Status.SUCCESS -> {
                if (response.data != null) {
                    if (activity != null) {
                        fillProfile(response.data)
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

//    var showView: MaterialShowcaseView? = null

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showPrompt("isFirstEnterMyProfileFragment", context.resources.getString(R.string.close_hint),
                getString(R.string.hint_create_travel), getString(R.string.hint_create_travel_description))

//        if ((activity as MenuActivity).preferences.getBoolean("isFirstEnterMyProfileFragment", true)) {
//            showView = MaterialShowcaseView.Builder(activity)
//                    .setTarget(newTripText)
//                    .renderOverNavigationBar()
//                    .setDismissText(getString(R.string.close_hint))
//                    .setTitleText(getString(R.string.hint_create_travel))
//                    .setContentText(getString(R.string.hint_create_travel_description))
//                    .withCircleShape()
//                    .setListener(object : IShowcaseListener {
//                        override fun onShowcaseDisplayed(p0: MaterialShowcaseView?) {
//                            val mHandler = Handler()
//                            val time = 10000L // 10 sec after we can hide tips
//                            try {
//                                mHandler.postDelayed({ hide() }, time)
//                            } catch (e: Exception) {
//                                e.printStackTrace()
//                            }
//                        }
//
//                        override fun onShowcaseDismissed(p0: MaterialShowcaseView?) {
//                            if (activity != null && !activity.isDestroyed) {
//                                (activity as MenuActivity).preferences.edit().putBoolean("isFirstEnterMyProfileFragment", false).apply()
//                            }
//                        }
//                    }).build()
//            showView?.show(activity)
        //       }
    }

//    private fun hide() {
//        try {
//            showView?.hide()
//        } catch (e: Exception) {
//            e.printStackTrace()
//            FirebaseCrash.report(e)
//        }
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("LOG code:", requestCode.toString() + " " +
                resultCode + " " + PlaceAutocomplete.getPlace(activity, data))

        // FIXME refactoring in viewModel

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

                        if (cityToLatLng.latitude != 0.0 && cityToLatLng.longitude != 0.0)
                            obtainDirection()
                        setMarkers(1)
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
                    textCityTo.setText(place.name)
                    textCityTo.error = null
                    cityToLatLng = GeoPoint(place.latLng.latitude, place.latLng.longitude)
                    if (cityToLatLng.hashCode() == cityFromLatLng.hashCode()) {
                        textCityTo.error = "true"
                        Toast.makeText(this@MyProfileFragment.context,
                                getString(R.string.fill_diff_cities), Toast.LENGTH_LONG).show()
                    } else {
                        cities[LAST_INDEX_CITY] = place.name.toString()
                        profileStateHashMap[CITY_TO] = cityToLatLng.hashCode().toString()
                        if (cityFromLatLng.latitude != 0.0 && cityFromLatLng.longitude != 0.0)
                            obtainDirection()
                        setMarkers(2)
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
        }
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

    private fun setMarkers(position: Int) {
        googleMap?.clear()
        val coordFrom = LatLng(cityFromLatLng.latitude, cityFromLatLng.longitude)
        val coordTo = LatLng(cityToLatLng.latitude, cityToLatLng.longitude)
        var markerTitleStart: String? = ""
        var markerTitleFinal: String? = ""
        var markerPositionStart = LatLng(0.0, 0.0)
        var markerPositionFinal = LatLng(0.0, 0.0)
        val cityFrom = cities[FIRST_INDEX_CITY]
        val cityTo = cities[LAST_INDEX_CITY]
        if (position == 1) {
            markerTitleStart = cityTo
            markerTitleFinal = cityFrom
            markerPositionStart = coordTo
            markerPositionFinal = coordFrom

        } else if (position == 2) {
            markerTitleFinal = cityTo
            markerTitleStart = cityFrom
            markerPositionFinal = coordTo
            markerPositionStart = coordFrom
        }

        val midPointLat = (coordFrom.latitude + coordTo.latitude) / 2
        val midPointLong = (coordFrom.longitude + coordTo.longitude) / 2
        val blueMarker = BitmapDescriptorFactory.fromResource(R.drawable.pin_blue)

        if (markerPositionStart != LatLng(0.0, 0.0)) {
            googleMap?.addMarker(MarkerOptions()
                    .icon(blueMarker)
                    .position(markerPositionStart)
                    .title(markerTitleStart)
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
                var perfectZoom = 190 / coordFrom.getBearing(coordTo)
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
        }

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
            openDialog(SocialNetwork.VK)
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_vk_click", null)
        }
        csIcon.setOnClickListener {
            openDialog(SocialNetwork.CS)
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_cs_click", null)
        }
        whatsAppIcon.setOnClickListener {
            openDialog(SocialNetwork.WHATSAPP)
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_wap_click", null)
        }
        fbcon.setOnClickListener {
            openDialog(SocialNetwork.FB)
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_fb_click", null)
        }
        tgIcon.setOnClickListener {
            openDialog(SocialNetwork.TG)
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_tg_click", null)
        }
        buttonSaveTravelInfo.setOnClickListener {
            Log.e("LOG", "save travel")
            sendUserInfoToServer()
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_save", null)
        }
        dateArrived.setOnClickListener {
            openDateArrivedDialog()
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_date_to_click", null)
        }
        textDateFrom.setOnClickListener {
            openDateFromDialog()
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_date_from_click", null)
        }
        addInfoUser.afterTextChanged({
            profileStateHashMap[ADD_INFO] = it
            profileChanged(null, false)
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_add_info", null)
            Log.d("LOG User", "after text change")
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
        buttonRemoveTravelInfo.setOnClickListener {
            openAlertDialog(this::removeTrip)
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_remove_trip", null)
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
        mapView?.onDestroy()
        //Clean up resources from google map to prevent memory leaks.
        //Stop tracking current location
        if (googleMap != null) {
            googleMap?.clear()
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

    private fun openDateFromDialog() {
        val dateFrom = Calendar.getInstance() //current time by default
        if (dates[START_DATE] ?: 0L > 0L) dateFrom.timeInMillis = dates[START_DATE] ?: dateFrom.timeInMillis

        dateDialog = CalendarDatePickerDialogFragment()
                .setFirstDayOfWeek(Calendar.MONDAY)
                .setThemeCustom(R.style.BythewayDatePickerDialogTheme)
                .setPreselectedDate(dateFrom.get(Calendar.YEAR), dateFrom.get(Calendar.MONTH), dateFrom.get(Calendar.DAY_OF_MONTH))

        dateDialog.setDateRange(MonthAdapter.CalendarDay(System.currentTimeMillis()), null)
        dateDialog.setOnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            textDateFrom.setText(StringBuilder(" ")
                    .append(dayOfMonth)
                    .append(" ")
                    .append(context.resources.getStringArray(R.array.months_array)[monthOfYear])
                    .append(" ")
                    .append(year).toString())
            dates[START_DATE] = getLongFromDate(dayOfMonth, monthOfYear, year)

            profileStateHashMap[DATES] = dates.toString()
            profileChanged()
        }
        dateDialog.show(activity.supportFragmentManager, "")
    }

    private fun openDateArrivedDialog() {
        val dateTo = Calendar.getInstance() //current time by default
        if (dates[END_DATE] ?: 0L > 0L) dateTo.timeInMillis = dates[END_DATE] ?: dateTo.timeInMillis

        dateDialog = CalendarDatePickerDialogFragment()
                .setFirstDayOfWeek(Calendar.MONDAY)
                .setThemeCustom(R.style.BythewayDatePickerDialogTheme)
                .setPreselectedDate(dateTo.get(Calendar.YEAR), dateTo.get(Calendar.MONTH), dateTo.get(Calendar.DAY_OF_MONTH))

        dateDialog.setDateRange(MonthAdapter.CalendarDay(dates[START_DATE]
                ?: System.currentTimeMillis()), null)
        dateDialog.setOnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            dateArrived.setText(StringBuilder(" ")
                    .append(dayOfMonth)
                    .append(" ")
                    .append(context.resources.getStringArray(R.array.months_array)[monthOfYear])
                    .append(" ")
                    .append(year).toString())
            dates[END_DATE] = getLongFromDate(dayOfMonth, monthOfYear, year)

            profileStateHashMap[DATES] = dates.toString()
            profileChanged()
        }
        dateDialog.show(activity.supportFragmentManager, "")
    }

    private fun sendUserInfoToServer() {

        var errorString = ""

        if (textCityFrom.text.isEmpty() || textCityTo.text.isEmpty()) {
            errorString = getString(R.string.fill_required_fields)
            if (textCityFrom.text.isEmpty()) {
                textCityFrom.error = getString(R.string.name)
                errorString += " " + getString(R.string.city_from)
            } else {
                textCityFrom.error = null
            }
            if (textCityTo.text.isEmpty()) {
                textCityTo.error = getString(R.string.yes)
                errorString += " " + getString(R.string.city_to)
            } else {
                textCityTo.error = null
            }
            textCityFrom.parent.requestChildFocus(textCityFrom, textCityFrom)
        }

        if (cityToLatLng.hashCode() == cityFromLatLng.hashCode()) {
            errorString += " " + getString(R.string.fill_diff_cities)
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_city_equals_err", null)
        }

        if (errorString.isNotEmpty()) {
            Toast.makeText(this@MyProfileFragment.context, errorString, Toast.LENGTH_LONG).show()
            return
        }

        countTrip = 1

        viewModel?.sendUserData(getHashMapUser(), uid, mainUser)
    }

    private fun showErrorResponse() {
        Toast.makeText(this@MyProfileFragment.context,
                getString(R.string.error_update), Toast.LENGTH_SHORT).show()
        mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_error_update", null)

    }

    private fun validCellPhone(number: String): Boolean {
        return number.matches(Regex("^([0-9]|\\+[0-9]){11,13}\$")) &&
                number != getString(R.string.default_phone_code)
    }

    private fun showBlockTravelInformation() {
        direction.visibility = View.VISIBLE
        maplayout.visibility = View.VISIBLE
        methodMoving.visibility = View.VISIBLE
        layoutTravelMethod.visibility = View.VISIBLE
        moneyfortrip.visibility = View.VISIBLE
        appinTheAirEnter.visibility = View.VISIBLE
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
        yearsView.setText(age.toString())
        yearsView.filters = arrayOf(DecimalInputFilter())

        simpleAlert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.save), { _, _ ->
            sex = if (man.isChecked) 1 else if (woman.isChecked) 2 else 0
            name = (nameChoose.text.toString()).capitalize()
            lastName = (lastNameChoose.text.toString()).capitalize()
            city = (cityChoose.text.toString()).capitalize()
            age = (yearsView.text.toStringOrNill()).toInt()

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

        val hashMap = HashMap<String, Any>()
        hashMap[NAME] = name
        hashMap[LASTNAME] = lastName
        hashMap[CITY] = city
        hashMap[AGE] = age
        hashMap[SEX] = sex

        viewModel?.sendUserData(hashMap, uid, mainUser)
    }

    private fun openDialog(socialNetwork: SocialNetwork, errorText: String? = null) {
        val simpleAlert = AlertDialog.Builder(activity).create()
        simpleAlert.setTitle(getString(R.string.social_links))
        simpleAlert.setMessage(getString(R.string.social_text))
        val dialogView = View.inflate(context, R.layout.custom_dialog_profile_soc_network, null)

        simpleAlert.setView(dialogView)
        var socVal = ""
        socialValues[socialNetwork.link]?.let {
            socVal = it
        }
        if (socialNetwork == SocialNetwork.TG && socVal.length <= 1) {
            socVal = numberPhone
        }
        dialogView.findViewById<EditText>(R.id.socLinkText).setText(socVal)

        if (errorText != null) {
            dialogView.findViewById<EditText>(R.id.socLinkText).error = errorText
        }

        simpleAlert.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.remove), { _, _ ->
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_remove_links", null)
            socNet.remove(socialNetwork.link)
            viewModel?.saveLinks(socNet, uid, SocialResponse(socialNetwork.link))
        })

        simpleAlert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.save), { _, _ ->
            val newLink = dialogView.findViewById<EditText>(R.id.socLinkText).text.toString()
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_save_links", null)

            var valid = true
            var errorText = ""

            if (socialNetwork == SocialNetwork.WHATSAPP) {
                valid = validCellPhone(newLink)
                if (!valid) errorText = getString(R.string.fill_phone_invalid)
            }

            if (!valid) {
                //openDialog(socialNetwork, errorText)
                dialogView.findViewById<EditText>(R.id.socLinkText).error = errorText
            } else {
                socNet[socialNetwork.link] = newLink
                viewModel?.saveLinks(socNet, uid, SocialResponse(socialNetwork.link, newLink)
                )
            }
        })

        dialogView.findViewById<EditText>(R.id.socLinkText).afterTextChanged {
            if (socialNetwork == SocialNetwork.WHATSAPP) {
                val valid = validCellPhone(it)
                if (!valid) {
                    dialogView.findViewById<EditText>(R.id.socLinkText).error =
                            getString(R.string.fill_phone_invalid)
                }
                simpleAlert.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = valid
            }
        }
        simpleAlert.show()
    }

    private fun openAlertDialog(callback: () -> Unit) {
        val simpleAlert = AlertDialog.Builder(activity).create()
        val dialogView = View.inflate(context, R.layout.confirm_dialog, null)

        simpleAlert.setView(dialogView)
        dialogView.textMessage.text = getString(R.string.text_confirm_remove_trip)
        simpleAlert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.yes), { _, _ ->
            callback()
        })
        simpleAlert.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.no), { _, _ ->
        })
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
        dateArrived.setText("")
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

    private fun fillProfile(user: User) {
        glide?.load(user.urlPhoto)
                ?.apply(RequestOptions.circleCropTransform())
                ?.into(image_avatar)



        cityFromLatLng = user.cityFromLatLng
        cityToLatLng = user.cityToLatLng

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

        if (user.cities.size > 0) {
            textCityFrom.setText(user.cities[FIRST_INDEX_CITY])
            textCityTo.setText(user.cities[LAST_INDEX_CITY])
            cities = user.cities
        }

        val formatDate = SimpleDateFormat("dd.MM.yyyy", Locale.US)

        if (user.dates.size > 0) {
            if (user.dates[START_DATE] != null && user.dates[START_DATE] != 0L) {
                textDateFrom.setText(formatDate.format(Date(user.dates[START_DATE] ?: 0)))
            }
            if (user.dates[END_DATE] != null && user.dates[END_DATE] != 0L)
                dateArrived.setText(formatDate.format(Date(user.dates[END_DATE] ?: 0)))
            dates = user.dates
        }

        fillAgeSex(user.age, user.sex)
        setMarkers(2)

        if (user.route.isNotBlank()) {
            routeString = user.route
            drawPolyline()
        } else
            obtainDirection()

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
            if (name.key in socialValues) {
                socialValues[name.key] = name.value
            }
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
    }

    private fun removeTrip() {
        countTrip = 0
        budget = 0
        methods.clear()
        cities.clear()
        //addInfoUser.setText("")
        cityFromLatLng = GeoPoint(0.0, 0.0)
        cityToLatLng = GeoPoint(0.0, 0.0)
        dates.clear()
        googleMap?.clear()
        routeString = ""
        viewModel?.sendUserData(getHashMapUser(), uid, mainUser)
        hideBlockTravelInforamtion()
        showBlockAddTrip()
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

        val hashMap = HashMap<String, Any>()

        hashMap[CITIES] = cities
        hashMap[METHOD] = methods
        hashMap[DATES] = dates
        hashMap[ROUTE] = routeString
        hashMap[BUDGET] = budget
        hashMap[BUDGET_POSITION] = budgetPosition
        hashMap[CITY_FROM] = cityFromLatLng
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
        viewModel?.getRoute(cityFromLatLng = cityFromLatLng, cityToLatLng = cityToLatLng)
    }
}

private fun Editable.toStringOrNill(): String = if (this.toString().isBlank()) "0" else this.toString()
