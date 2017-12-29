package ru.a1024bits.bytheway.ui.fragments


import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import com.borax12.materialdaterangepicker.date.DatePickerDialog
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.fragment_search_block.*
import kotlinx.android.synthetic.main.fragment_user_profile.*
import kotlinx.android.synthetic.main.profile_direction.*
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.model.Method
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.repository.Filter
import ru.a1024bits.bytheway.router.OnFragmentInteractionListener
import ru.a1024bits.bytheway.util.Constants
import ru.a1024bits.bytheway.util.Constants.END_DATE
import ru.a1024bits.bytheway.util.Constants.FIRST_INDEX_CITY
import ru.a1024bits.bytheway.util.Constants.LAST_INDEX_CITY
import ru.a1024bits.bytheway.util.Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_FROM
import ru.a1024bits.bytheway.util.Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_TO
import ru.a1024bits.bytheway.util.Constants.START_DATE
import ru.a1024bits.bytheway.util.DecimalInputFilter
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by andrey.gusenkov on 29/09/2017.
 */
class SearchFragment : Fragment(), DatePickerDialog.OnDateSetListener {

    private lateinit var dateDialog: DatePickerDialog
    var firstPoint: LatLng? = null
    var secondPoint: LatLng? = null
    var user: User = User()
    var filter: Filter = Filter()

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
        filter.startDate = user.dates.get(START_DATE) ?: 0
        filter.endDate = user.dates.get(END_DATE) ?: 0
        filter.endBudget = user.budget.toInt()
        filter.method = user.method
        filter.locationStartCity = LatLng(user.cityFromLatLng.latitude, user.cityFromLatLng.longitude)
        filter.locationEndCity = LatLng(user.cityToLatLng.latitude, user.cityToLatLng.longitude)
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        for (method in user.method.keys) {
            when (method) {
                Method.CAR.link -> {
                    iconCar.isActivated = user.method.get(method) == true
                }
                Method.TRAIN.link -> {
                    iconTrain.isActivated = user.method.get(method) == true
                }
                Method.BUS.link -> {
                    iconBus.isActivated = user.method.get(method) == true
                }
                Method.PLANE.link -> {
                    iconPlane.isActivated = user.method.get(method) == true
                }
                Method.HITCHHIKING.link -> {
                    iconHitchHicking.isActivated = user.method.get(method) == true
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

        text_from_city.text = user.cities.get(FIRST_INDEX_CITY)
        text_from_city.setOnClickListener {
            sendIntentForSearch(PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_FROM)
        }

        text_to_city.text = user.cities.get(LAST_INDEX_CITY)
        text_to_city.setOnClickListener {
            sendIntentForSearch(PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_TO)
        }
        if (user.dates.size > 0) {
            dateFromValue.text = user.dates.get(START_DATE)?.getNormallDate()
            dateToValue.text = user.dates.get(END_DATE)?.getNormallDate()
        }
        
        swap_cities.setOnClickListener {
            val tempString = text_from_city.text
            text_from_city.text = text_to_city.text
            text_to_city.text = tempString
            //fixme
        }

        budgetFromValue.setText(user.budget.toString())
        budgetFromValue.filters = arrayOf(DecimalInputFilter())
        budgetToValue.filters = arrayOf(DecimalInputFilter()) //fixme textWatcher
        if (user.cityFromLatLng.latitude != 0.0 && user.cityToLatLng.latitude != 0.0 && user.cityToLatLng.longitude != 0.0) {
            firstPoint = LatLng(user.cityFromLatLng.latitude, user.cityFromLatLng.longitude)
            secondPoint = LatLng(user.cityToLatLng.latitude, user.cityToLatLng.longitude)
            firstPoint?.let { latLng -> (activity as OnFragmentInteractionListener).onSetPoint(latLng, 1) }
            secondPoint?.let { latLng -> (activity as OnFragmentInteractionListener).onSetPoint(latLng, 2) }
        }
    }

    private fun openDateDialog() {
        dateDialog.setStartTitle(getString(R.string.date_start))
        dateDialog.setEndTitle(getString(R.string.date_end))
        dateDialog.accentColor = resources.getColor(R.color.colorPrimary)
        dateDialog.show(activity.fragmentManager, "")
    }

    override fun onDateSet(view: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int, yearEnd: Int, monthOfYearEnd: Int, dayOfMonthEnd: Int) {
        Log.e("LOG Date", "$year  $monthOfYear $dayOfMonth - $yearEnd $monthOfYearEnd $dayOfMonthEnd")
        dateFromValue.setText(StringBuilder(" ")
                .append(dayOfMonth)
                .append(" ")
                .append(context.resources.getStringArray(R.array.months_array)[monthOfYear])
                .append(" ")
                .append(year).toString())

        dateToValue.setText(StringBuilder(" ")
                .append(dayOfMonthEnd)
                .append(" ")
                .append(context.resources.getStringArray(R.array.months_array)[monthOfYearEnd])
                .append(" ")
                .append(yearEnd).toString())
        filter.startDate = getLongFromDate(dayOfMonth, monthOfYear, year)
        filter.endDate = getLongFromDate(dayOfMonthEnd, monthOfYearEnd, yearEnd)
    }

    private fun getLongFromDate(day: Int, month: Int, year: Int): Long {
        val dateString = "$day $month $year"
        val dateFormat = SimpleDateFormat("dd MM yyyy")
        val date = dateFormat.parse(dateString)
        val unixTime = date.time.toLong()
        return unixTime
    }

    override fun onStart() {
        super.onStart()
        val now = Calendar.getInstance()

        dateDialog = DatePickerDialog.newInstance(
                this@SearchFragment,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        )
        dateFromValue.setOnClickListener {
            openDateDialog()
        }
        dateToValue.setOnClickListener {
            openDateDialog()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("LOG code:", requestCode.toString() + " " + resultCode + " " + PlaceAutocomplete.getPlace(activity, data))

        // FIXME refactoring in viewModel

        when (requestCode) {
            PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_FROM -> when (resultCode) {
                AppCompatActivity.RESULT_OK -> {
                    val place = PlaceAutocomplete.getPlace(activity, data);
                    text_from_city.text = place.name
                    firstPoint = place.latLng
                    filter.locationEndCity = place.latLng
                    text_from_city.error = null
                    if (secondPoint != null && firstPoint?.latitude == secondPoint?.latitude) {
                        text_from_city.error = "true"
                        Toast.makeText(this@SearchFragment.context, getString(R.string.fill_diff_cities), Toast.LENGTH_SHORT).show()
                    } else {
                        text_to_city.error = null
                        text_from_city.error = null
                    }
                    firstPoint?.let { latLng -> (activity as OnFragmentInteractionListener).onSetPoint(latLng, 1) }
                }
                else -> {
                    val status = PlaceAutocomplete.getStatus(activity, data)
                    Log.i("LOG", status.getStatusMessage() + " ")
                    text_from_city.text = ""
                }
            }

            PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_TO -> when (resultCode) {
                AppCompatActivity.RESULT_OK -> {
                    val place = PlaceAutocomplete.getPlace(activity, data);
                    text_to_city.text = place.name
                    secondPoint = place.latLng
                    filter.locationStartCity = place.latLng

                    text_to_city.error = null
                    if (firstPoint != null && firstPoint?.latitude == secondPoint?.latitude) {
                        text_to_city.error = "true"
                        Toast.makeText(this@SearchFragment.context, getString(R.string.fill_diff_cities), Toast.LENGTH_SHORT).show()
                    } else {
                        text_from_city.error = null
                        text_to_city.error = null
                    }
                    secondPoint?.let { latLng -> (activity as OnFragmentInteractionListener).onSetPoint(latLng, 2) }
                }
                else -> {
                    val status = PlaceAutocomplete.getStatus(activity, data)
                    Log.i("LOG", status.statusMessage + " ")
                    text_to_city.text = ""
                }
            }
        }
    }

    private fun sendIntentForSearch(code: Int) {
        try {
            val typeFilter = AutocompleteFilter.Builder()
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                    .build()
            val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).setFilter(typeFilter).build(activity)
            startActivityForResult(intent, code)
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        }
    }
}

private fun Long.getNormallDate(): CharSequence? = SimpleDateFormat("dd.MM.yyyy").format(this)
