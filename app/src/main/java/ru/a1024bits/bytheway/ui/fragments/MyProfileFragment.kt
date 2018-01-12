package ru.a1024bits.bytheway.ui.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
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
import com.borax12.materialdaterangepicker.date.DatePickerDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
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
import ru.a1024bits.bytheway.util.Constants.END_DATE
import ru.a1024bits.bytheway.util.Constants.FIRST_INDEX_CITY
import ru.a1024bits.bytheway.util.Constants.LAST_INDEX_CITY
import ru.a1024bits.bytheway.util.Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_FROM
import ru.a1024bits.bytheway.util.Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_TO
import ru.a1024bits.bytheway.util.Constants.START_DATE
import ru.a1024bits.bytheway.util.getBearing
import ru.a1024bits.bytheway.viewmodel.MyProfileViewModel
import ru.terrakok.cicerone.commands.Replace
import uk.co.deanwild.materialshowcaseview.IShowcaseListener
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap
import ru.a1024bits.bytheway.model.Response as ResponseBtw


class MyProfileFragment : BaseFragment<MyProfileViewModel>(), OnMapReadyCallback, DatePickerDialog.OnDateSetListener {

    companion object {
        val BUDGET = "budget"
        val BUDGET_POSITION = "budgetPosition"
        val CITIES = "cities"
        val METHOD = "method"
        val METHODS = "methods"
        val DATES = "dates"
        val CITY_FROM = "cityFromLatLng"
        val CITY_TO = "cityToLatLng"
        val ADD_INFO = "addInformation"
        val COUNT_TRIP = "countTrip"
        val SEX = "sex"
        val AGE = "age"
        val NAME = "name"
        val LASTNAME = "lastName"
        val CITY = "city"
    }

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    private var mListener: OnFragmentInteractionListener? = null

    private var uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    private var name = ""
    private var cityFromLatLng = GeoPoint(0.0, 0.0)
    private var cityToLatLng = GeoPoint(0.0, 0.0)
    private var lastName = ""
    private var city = ""
    private lateinit var dateDialog: DatePickerDialog
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
    private var yearNow: Int = 0
    private var yearsArr: ArrayList<Int> = arrayListOf()
    private var profileStateHashMap: HashMap<String, String> = hashMapOf()
    private var oldProfileState: Int = 0
    private var routeString: String = ""
    private var routes: String = ""
    private var googleMap: GoogleMap? = null
    private var mapView: MapView? = null
    private var countTrip: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component.inject(this)
        (activity as MenuActivity).pLoader?.show()
    }

    private val routerObserver: Observer<ru.a1024bits.bytheway.model.Response<RoutesList>> =
            Observer<ru.a1024bits.bytheway.model.Response<RoutesList>> { response ->
                when (response?.status) {
                    Status.SUCCESS -> {
                        if (response.data != null) {
                            if (activity != null) {
                                response.data.routes?.map {
                                    it.overviewPolyline?.encodedData?.let { routeString ->
                                        this@MyProfileFragment.routeString = routeString
                                        drawPolyline()
                                    }
                                }
                            }
                        }
                    }
                    Status.ERROR -> {
                        showErrorRouter()
                    }
                }
            }

    private fun showErrorRouter() {
        mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_error_router", null)
        Log.d("LOG", "error  get routing")
    }

    private val responseObserver: Observer<ru.a1024bits.bytheway.model.Response<User>> =
            Observer<ru.a1024bits.bytheway.model.Response<User>> { response ->
                when (response?.status) {
                    Status.SUCCESS -> {
                        if (response.data != null) {
                            if (activity != null) {
                                (activity as MenuActivity).pLoader?.hide()
                                fillProfile(response.data)
                                mListener?.onFragmentInteraction(response.data)
                            }
                        }
                    }
                    Status.ERROR -> {
                        showErrorResponse()
                        Log.e("LOG", "log e:" + response.error)
                    }
                }
            }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        App.component.inject(this)
        glide = Glide.with(this)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context)

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

        viewModel?.routes?.observe(this, routerObserver)
        viewModel?.response?.observe(this, responseObserver)
    }

    override fun getViewFactoryClass(): ViewModelProvider.Factory = viewModelFactory

    @LayoutRes
    override fun getLayoutRes(): Int {
        return R.layout.fragment_user_profile
    }

    override fun getViewModelClass(): Class<MyProfileViewModel> = MyProfileViewModel::class.java

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if ((activity as MenuActivity).preferences.getBoolean("isFirstEnterMyProfileFragment", true)) {
            MaterialShowcaseView.Builder(activity)
                    .setTarget(newTripText)
                    .renderOverNavigationBar()
                    .setDismissText(getString(R.string.close_hint))
                    .setTitleText(getString(R.string.hint_create_travel))
                    .setContentText(getString(R.string.hint_create_travel_description))
                    .withCircleShape()
                    .setListener(object : IShowcaseListener {
                        override fun onShowcaseDisplayed(p0: MaterialShowcaseView?) {}
                        override fun onShowcaseDismissed(p0: MaterialShowcaseView?) {
                            if (activity != null && !activity.isDestroyed) {
                                (activity as MenuActivity).preferences.edit().putBoolean("isFirstEnterMyProfileFragment", false).apply()
                            }
                        }
                    }).show()
        }
    }

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
                    if (cityToLatLng.hashCode() == cityFromLatLng.hashCode()) {
                        textCityFrom.error = "true"

                        Toast.makeText(this@MyProfileFragment.context,
                                getString(R.string.fill_diff_cities), Toast.LENGTH_LONG).show()
                    } else {
                        cities.put(FIRST_INDEX_CITY, place.name.toString())
                        profileStateHashMap[CITY_FROM] = cityFromLatLng.hashCode().toString()

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
                        cities.put(LAST_INDEX_CITY, place.name.toString())
                        profileStateHashMap[CITY_TO] = cityToLatLng.hashCode().toString()
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
        mapView?.onResume()
    }

    override fun onMapReady(map: GoogleMap?) {
        this.googleMap = map
        viewModel?.load(uid)
        Log.d("LOG", " load map ready")
    }

    private fun setMarkers(position: Int) {
        googleMap?.clear()
        val coordFrom = LatLng(cityFromLatLng.latitude, cityFromLatLng.longitude)
        val coordTo = LatLng(cityToLatLng.latitude, cityToLatLng.longitude)
        var markerTitleStart: String? = ""
        var markerTitleFinal: String? = ""
        var markerPositionStart = LatLng(0.0, 0.0)
        var markerPositionFinal = LatLng(0.0, 0.0)
        val cityFrom = cities.get(FIRST_INDEX_CITY)
        val cityTo = cities.get(LAST_INDEX_CITY)
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

        var perfectZoom = 190 / coordFrom.getBearing(coordFrom)
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(midPointLat, midPointLong), perfectZoom))
    }

    fun drawPolyline() {

        val blueColor = activity.resources.getColor(R.color.blueRouteLine)

        var polyPts: List<LatLng>
        val options = PolylineOptions()
        options.color(blueColor)

        options.width(5f)

        if (routeString != "") {
            polyPts = PolyUtil.decode(routeString)

            for (pts in polyPts) {
                options.add(pts)
            }
        }

        googleMap?.addPolyline(options)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_my_user_profile, container, false)
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

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    private val TAG_ANALYTICS: String = "MProfile_screen"

    override fun onStart() {
        super.onStart()

        displayPriceTravel.text = StringBuilder(getString(R.string.type_money)).append(budget)

        val now = Calendar.getInstance()

        dateDialog = DatePickerDialog.newInstance(
                this@MyProfileFragment,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        )

        headerprofile.setOnClickListener {
            openInformationEditDialog()
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_header_click", null)
        }

        choosePriceTravel.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, number: Int, p2: Boolean) {
                budget = (150 * number).toLong()
                displayPriceTravel.text = StringBuilder(getString(R.string.type_money)).append(budget)
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
            openDateDialog()
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_date_to_click", null)

        }
        textDateFrom.setOnClickListener {
            openDateDialog()
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_date_from_click", null)
        }
        addInfoUser.afterTextChanged({
            profileStateHashMap[ADD_INFO] = it
            profileChanged(null, false)
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_click_air", null)
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

        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onDestroy() {

        mapView?.onDestroy()
        //Clean up resources from google map to prevent memory leaks.
        //Stop tracking current location
        if (googleMap != null) {
            googleMap?.clear()
        }
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDateSet(view: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int, yearEnd: Int, monthOfYearEnd: Int, dayOfMonthEnd: Int) {
        Log.e("LOG Date", "$year  $monthOfYear $dayOfMonth - $yearEnd $monthOfYearEnd $dayOfMonthEnd")
        textDateFrom.setText(StringBuilder(" ")
                .append(dayOfMonth)
                .append(" ")
                .append(context.resources.getStringArray(R.array.months_array)[monthOfYear])
                .append(" ")
                .append(year).toString())

        dateArrived.setText(StringBuilder(" ")
                .append(dayOfMonthEnd)
                .append(" ")
                .append(context.resources.getStringArray(R.array.months_array)[monthOfYearEnd])
                .append(" ")
                .append(yearEnd).toString())


        dates.put(START_DATE, getLongFromDate(dayOfMonth, monthOfYear, year))
        dates.put(END_DATE, getLongFromDate(dayOfMonthEnd, monthOfYearEnd, yearEnd))
        profileStateHashMap[DATES] = dates.toString()
        profileChanged()
    }

    private fun getLongFromDate(day: Int, month: Int, year: Int): Long {
        val dateString = "$day $month $year"
        val dateFormat = SimpleDateFormat("dd MM yyyy", Locale.US)
        val date = dateFormat.parse(dateString)
        return date.time

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

    private fun openDateDialog() {
        dateDialog.setStartTitle(resources.getString(R.string.date_start))
        dateDialog.setEndTitle(resources.getString(R.string.date_end))
        dateDialog.accentColor = ContextCompat.getColor(context, R.color.colorPrimary)
        dateDialog.show(activity.fragmentManager, "")
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

        if (viewModel?.saveProfile?.hasObservers() == false) {
            viewModel?.saveProfile?.observe(this, Observer<ResponseBtw<Boolean>> { response ->
                when (response?.status) {
                    Status.SUCCESS -> {
                        if (response.data == true) {
                            if (activity != null) {
                                Toast.makeText(this@MyProfileFragment.context, resources.getString(R.string.save_succesfull), Toast.LENGTH_SHORT).show()
                                profileChanged(false)
                                (activity as MenuActivity).pLoader?.hide()
                            }
                        } else {
                            showErrorResponse()
                        }
                    }
                    Status.ERROR -> {
                        showErrorResponse()
                        Log.e("LOG", "log e:" + response.error)
                    }
                }
            })
        }
        (activity as MenuActivity).pLoader?.show()
        countTrip = 1

        viewModel?.sendUserData(getHashMapUser(), uid)
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

        val spinnerYearsView = dialogView.findViewById<View>(R.id.spinnerYearsView) as Spinner

        val man = dialogView.findViewById<RadioButton>(R.id.man)
        val woman = dialogView.findViewById<RadioButton>(R.id.woman)

        val sexChoose = dialogView.findViewById<RadioGroup>(R.id.sex)
        if (sex == 1) {
            sexChoose.check(man.id)
        } else if (sex == 2) {
            sexChoose.check(woman.id)
        }

        val calendar = Calendar.getInstance()
        yearNow = calendar.get(Calendar.YEAR)

        for (i in 1920..yearNow) yearsArr.add(i)

        val yearsAdapter = ArrayAdapter<Int>(this.context, android.R.layout.simple_spinner_item, yearsArr)

        yearsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerYearsView.adapter = yearsAdapter
        spinnerYearsView.prompt = getString(R.string.date)

        spinnerYearsView.setSelection(yearsArr.size - age - 1)
        spinnerYearsView.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString().toLong()
                age = (yearNow - selectedItem).toInt()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                Log.e("LOG", "Nothing 2")
            }
        }
        simpleAlert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.save), { _, _ ->
            sex = if (man.isChecked) 1 else if (woman.isChecked) 2 else 0
            fillAgeSex(age, sex)
            name = (nameChoose.text.toString()).capitalize()
            lastName = (lastNameChoose.text.toString()).capitalize()
            city = (cityChoose.text.toString()).capitalize()

            username.text = StringBuilder(name).append(" ").append(lastName)
            cityview.text = if (city.isNotEmpty()) city else getString(R.string.native_city)
            viewModel?.sendUserData(getHashMapUser(), uid)

            savingUserData(name, lastName, city)

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

    private fun savingUserData(name: String, lastName: String, city: String) {
        username.text = StringBuilder(name).append(" ").append(lastName)
        cityview.text = if (city.isNotEmpty()) city else getString(R.string.native_city)
        viewModel?.sendUserData(getHashMapUser(), uid)
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
        if (viewModel?.saveSocial?.hasObservers() == false) {
            viewModel?.saveSocial?.observe(this, Observer<SocialResponse> { response ->
                response?.let {
                    changeSocIconsActive(response.link, response.value)
                }
            })
        }
        if (viewModel?.clearSocial?.hasObservers() == false) {
            viewModel?.clearSocial?.observe(this, Observer<SocialResponse> { response ->
                response?.let {
                    changeSocIconsDisActive(response.link)
                }
            })
        }
        simpleAlert.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.remove), { _, _ ->
            mFirebaseAnalytics.logEvent("${TAG_ANALYTICS}_remove_links", null)
            viewModel?.saveLinks(socNet, uid, {
                viewModel?.clearSocial?.setValue(SocialResponse(socialNetwork.link))
            })
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
                openDialog(socialNetwork, errorText)
            } else {
                socNet.put(socialNetwork.link, newLink)
                viewModel?.saveLinks(socNet, uid, {
                    viewModel?.saveSocial?.setValue(SocialResponse(socialNetwork.link, newLink))
                })
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
        val navigationView = activity.findViewById<NavigationView>(R.id.nav_view)
        val hView = navigationView.getHeaderView(0)
        val cityName = hView.findViewById<TextView>(R.id.menu_city_name)
        cityName.text = user.city
        val fullName = hView.findViewById<TextView>(R.id.menu_fullname)
        fullName.text = StringBuilder().append(user.name).append(" ").append(user.lastName)
        username.text = StringBuilder(user.name).append(" ").append(user.lastName)

        routes = user.route
        cityFromLatLng = user.cityFromLatLng
        cityToLatLng = user.cityToLatLng

        profileStateHashMap.clear()
        lastName = user.lastName
        name = user.name
        numberPhone = user.phone


        user.socialNetwork.map {
            if (it.key in socialValues) {
                socialValues[it.key] = it.value
            }
        }

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
            val dayBegin = formatDate.format(Date(user.dates[START_DATE] ?: 0))
            val dayArrival = formatDate.format(Date(user.dates[END_DATE] ?: 0))
            textDateFrom.setText(dayBegin)
            dateArrived.setText(dayArrival)
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

        glide?.load(user.urlPhoto)
                ?.apply(RequestOptions.circleCropTransform())
                ?.into(image_avatar)

        for (name in user.socialNetwork) {
            socNet.put(name.key, name.value)
            when (name.key) {
                SocialNetwork.VK.link -> vkIcon.setImageResource(R.drawable.ic_vk_color)
                SocialNetwork.CS.link -> csIcon.setImageResource(R.drawable.ic_cs_color)
                SocialNetwork.FB.link -> fbcon.setImageResource(R.drawable.ic_fb_color)
                SocialNetwork.WHATSAPP.link -> whatsAppIcon.setImageResource(R.drawable.ic_whats_icon_color)
                SocialNetwork.TG.link -> tgIcon.setImageResource(R.drawable.ic_tg_color)
            }
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
    }

    private fun removeTrip() {
        countTrip = 0
        budget = 0
        methods.clear()
        cities.clear()
        dates.clear()
        viewModel?.sendUserData(getHashMapUser(), uid)
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
        hashMap[BUDGET] = budget
        hashMap[BUDGET_POSITION] = budgetPosition
        hashMap[CITY_FROM] = cityFromLatLng
        hashMap[CITY_TO] = cityToLatLng
        hashMap[ADD_INFO] = addInfoUser.text.toString()
        hashMap.put(COUNT_TRIP, countTrip)
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