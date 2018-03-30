package ru.a1024bits.bytheway.ui.fragments


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment
import com.codetroopers.betterpickers.calendardatepicker.MonthAdapter
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crash.FirebaseCrash
import kotlinx.android.synthetic.main.fragment_search_block.*
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.model.Method
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.Filter
import ru.a1024bits.bytheway.router.OnFragmentInteractionListener
import ru.a1024bits.bytheway.util.Constants.END_DATE
import ru.a1024bits.bytheway.util.Constants.FIRST_INDEX_CITY
import ru.a1024bits.bytheway.util.Constants.LAST_INDEX_CITY
import ru.a1024bits.bytheway.util.Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_FROM
import ru.a1024bits.bytheway.util.Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_TO
import ru.a1024bits.bytheway.util.Constants.START_DATE
import ru.a1024bits.bytheway.util.DateUtils.Companion.getLongFromDate
import ru.a1024bits.bytheway.util.DecimalInputFilter
import ru.a1024bits.bytheway.util.getIntOrNothing
import ru.a1024bits.bytheway.util.getNormallDate
import ru.a1024bits.bytheway.util.toStringOrEmpty
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by andrey.gusenkov on 29/09/2017
 */
class SearchFragment : Fragment() {

    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
    private lateinit var dateDialog: CalendarDatePickerDialogFragment
    private var firstPoint: LatLng? = null
    private var secondPoint: LatLng? = null
    var user: User = User()
    var filter: Filter = Filter()

    private val FIRST_MARKER_POSITION: Int = 1

    private val SECOND_MARKER_POSITION: Int = 2

    companion object {
        fun newInstance(user: User?): SearchFragment {
            val fragment = SearchFragment()
            fragment.arguments = Bundle()
            fragment.user = user ?: User()
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_search_block, container, false)
        activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context)
        try {
            filter.startDate = user.dates[START_DATE] ?: 0
            filter.endDate = user.dates[END_DATE] ?: 0
            filter.endBudget = user.budget.toInt()
            filter.method = user.method
            filter.endCity = user.cities[LAST_INDEX_CITY] ?: ""
            filter.startCity = user.cities[FIRST_INDEX_CITY] ?: ""
            filter.locationStartCity = LatLng(user.cityFromLatLng.latitude, user.cityFromLatLng.longitude)
            filter.locationEndCity = LatLng(user.cityToLatLng.latitude, user.cityToLatLng.longitude)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        for (method in user.method.keys) {
            when (method) {
                Method.CAR.link -> {
                    iconCar.isActivated = user.method[method] == true
                }
                Method.TRAIN.link -> {
                    iconTrain.isActivated = user.method[method] == true
                }
                Method.BUS.link -> {
                    iconBus.isActivated = user.method[method] == true
                }
                Method.PLANE.link -> {
                    iconPlane.isActivated = user.method[method] == true
                }
                Method.HITCHHIKING.link -> {
                    iconHitchHicking.isActivated = user.method[method] == true
                }
            }
        }
        iconCar.setOnClickListener {
            with(travelCarText) {
                isActivated = !isActivated
                filter.method.put(Method.CAR.link, isActivated)
            }
        }
        iconTrain.setOnClickListener {
            with(travelTrainText) {
                isActivated = !isActivated
                filter.method.put(Method.TRAIN.link, isActivated)
            }
        }
        iconBus.setOnClickListener {
            with(travelBusText) {
                isActivated = !isActivated
                filter.method.put(Method.BUS.link, isActivated)

            }
        }
        iconPlane.setOnClickListener {
            with(travelPlaneText) {
                isActivated = !isActivated
                filter.method.put(Method.PLANE.link, isActivated)
            }
        }
        iconHitchHicking.setOnClickListener {
            with(travelHitchHikingText) {
                isActivated = !isActivated
                filter.method.put(Method.HITCHHIKING.link, isActivated)
            }
        }

        text_from_city.text = user.cities[FIRST_INDEX_CITY]
        text_from_city.setOnClickListener {
            sendIntentForSearch(PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_FROM)
        }

        text_to_city.text = user.cities[LAST_INDEX_CITY]
        text_to_city.setOnClickListener {
            sendIntentForSearch(PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_TO)
        }

        if (user.dates.size > 0) {
            if (user.dates[START_DATE] != null && user.dates[START_DATE] != 0L)
                dateFromValue.text = user.dates[START_DATE]?.getNormallDate()
            if (user.dates[END_DATE] != null && user.dates[END_DATE] != 0L)
                dateToValue.text = user.dates[END_DATE]?.getNormallDate()
        }



        swap_cities.setOnClickListener {
            val tempString = text_from_city.text
            text_from_city.text = text_to_city.text
            text_to_city.text = tempString

            filter.endCity = text_to_city.text.toString()
            filter.startCity = text_from_city.text.toString()

            val tempLng = filter.locationStartCity
            filter.locationStartCity = filter.locationEndCity
            filter.locationEndCity = tempLng

            updatePoints(filter.locationStartCity, filter.locationEndCity, swap = true)
        }

        budgetFromValue.setText(user.budget.toStringOrEmpty)
        budgetFromValue.filters = arrayOf(DecimalInputFilter())
        budgetFromValue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(text: Editable?) {
                filter.endBudget = text.toString().getIntOrNothing()
            }

        })
        if (user.cityFromLatLng.latitude != 0.0 && user.cityToLatLng.latitude != 0.0 && user.cityToLatLng.longitude != 0.0) {
            // ставим маркеры на карту
            updatePoints(LatLng(user.cityFromLatLng.latitude, user.cityFromLatLng.longitude), LatLng(user.cityToLatLng.latitude, user.cityToLatLng.longitude))
        }
    }

    private fun updatePoints(start: LatLng, end: LatLng, swap: Boolean = false) {
        firstPoint = start
        secondPoint = end
        firstPoint?.let { latLng ->
            (activity as OnFragmentInteractionListener).onSetPoint(latLng, FIRST_MARKER_POSITION, swap)
        }
        secondPoint?.let { latLng ->
            (activity as OnFragmentInteractionListener).onSetPoint(latLng, SECOND_MARKER_POSITION, swap)
        }
    }

    private fun openDateFromDialog() {

        val dateFrom = Calendar.getInstance() //current time by default
        if (filter.startDate > 0L) dateFrom.timeInMillis = filter.startDate

        dateDialog = CalendarDatePickerDialogFragment()
                .setFirstDayOfWeek(Calendar.MONDAY)
                .setThemeCustom(R.style.BythewayDatePickerDialogTheme)
                .setPreselectedDate(dateFrom.get(Calendar.YEAR), dateFrom.get(Calendar.MONTH), dateFrom.get(Calendar.DAY_OF_MONTH))

        dateDialog.setDateRange(MonthAdapter.CalendarDay(System.currentTimeMillis()), null)
        dateDialog.setOnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            dateFromValue?.text = StringBuilder(" ")
                    .append(dayOfMonth)
                    .append(" ")
                    .append(context.resources.getStringArray(R.array.months_array)[monthOfYear])
                    .append(" ")
                    .append(year).toString()
            filter.startDate = getLongFromDate(dayOfMonth, monthOfYear, year)
        }
        dateDialog.show(activity.supportFragmentManager, "")
    }

    private fun openDateToDialog() {
        val dateTo = Calendar.getInstance() //current time by default
        if (filter.endDate > 0L) dateTo.timeInMillis = filter.endDate

        dateDialog = CalendarDatePickerDialogFragment()
                .setFirstDayOfWeek(Calendar.MONDAY)
                .setThemeCustom(R.style.BythewayDatePickerDialogTheme)
                .setPreselectedDate(dateTo.get(Calendar.YEAR), dateTo.get(Calendar.MONTH), dateTo.get(Calendar.DAY_OF_MONTH))

        dateDialog.setDateRange(
                MonthAdapter.CalendarDay(
                        if (filter.startDate > 0L) filter.startDate
                        else System.currentTimeMillis()), null)
        dateDialog.setOnDateSetListener { _, year, monthOfYear, dayOfMonth ->

            dateToValue?.text = StringBuilder(" ")
                    .append(dayOfMonth)
                    .append(" ")
                    .append(context.resources.getStringArray(R.array.months_array)[monthOfYear])
                    .append(" ")
                    .append(year).toString()
            filter.endDate = getLongFromDate(dayOfMonth, monthOfYear, year)
        }
        dateDialog.show(activity.supportFragmentManager, "")
    }


    override fun onStart() {
        super.onStart()
        val now = Calendar.getInstance()

        dateDialog = CalendarDatePickerDialogFragment()
                .setFirstDayOfWeek(Calendar.MONDAY)
                .setThemeCustom(R.style.BythewayDatePickerDialogTheme)
                .setPreselectedDate(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH))

        dateFromValue.setOnClickListener {
            openDateFromDialog()
            mFirebaseAnalytics.logEvent("Search_fragment_str_date_dialog", null)
        }

        dateToValue.setOnClickListener {
            openDateToDialog()
            mFirebaseAnalytics.logEvent("Search_fragment_end_date_dialog", null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // FIXME refactoring in viewModel

        when (requestCode) {
            PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_FROM -> when (resultCode) {
                AppCompatActivity.RESULT_OK -> {
                    val place = PlaceAutocomplete.getPlace(activity, data)
                    text_from_city?.text = place.name
                    firstPoint = place.latLng
                    filter.startCity = place.name.toString()
                    filter.locationStartCity = place.latLng
                    text_from_city?.error = null
                    manageErrorCityEquals(secondPoint)
                    firstPoint?.let { latLng -> (activity as OnFragmentInteractionListener).onSetPoint(latLng, FIRST_MARKER_POSITION) }
                }
                else -> {
                    val status = PlaceAutocomplete.getStatus(activity, data)
                    Log.i("LOG", status.statusMessage + " ")
                    if (text_from_city != null) {
                        text_from_city.text = filter.startCity
                    }
                }
            }

            PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_TO -> when (resultCode) {
                AppCompatActivity.RESULT_OK -> {
                    try {
                        val place = PlaceAutocomplete.getPlace(activity, data)
                        text_to_city?.text = place.name
                        secondPoint = place.latLng
                        filter.locationEndCity = place.latLng
                        filter.endCity = place.name.toString()
                        text_to_city?.error = null
                        manageErrorCityEquals(firstPoint)
                        secondPoint?.let { latLng ->
                            (activity as OnFragmentInteractionListener).onSetPoint(latLng, SECOND_MARKER_POSITION)
                        }
                    }catch (e: Exception) {
                        e.printStackTrace()
                        FirebaseCrash.report(e)
                    }
                }
                else -> {
                    val status = PlaceAutocomplete.getStatus(activity, data)
                    Log.i("LOG", status.statusMessage + " ")
                    if (text_to_city != null)
                        text_to_city.text = filter.endCity
                }
            }
        }
    }

    private fun manageErrorCityEquals(point: LatLng?) {
        if (point != null && this.firstPoint?.latitude == this.secondPoint?.latitude && this.firstPoint?.longitude == this.secondPoint?.longitude) {
            text_to_city?.error = "true"
            text_from_city?.error = "true"
            Toast.makeText(this@SearchFragment.context, getString(R.string.fill_diff_cities), Toast.LENGTH_SHORT).show()
        } else {
            text_from_city?.error = null
            text_to_city?.error = null
        }
    }

    private fun sendIntentForSearch(code: Int) {
        try {
            val typeFilter = AutocompleteFilter.Builder()
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                    .build()
            val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).setFilter(typeFilter).build(activity)
            startActivityForResult(intent, code)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}