package ru.a1024bits.bytheway.ui.fragments


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.borax12.materialdaterangepicker.date.DatePickerDialog
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.fragment_search_block.*
import kotlinx.android.synthetic.main.profile_direction.*
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.router.OnFragmentInteractionListener
import ru.a1024bits.bytheway.util.Constants
import ru.a1024bits.bytheway.util.Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_FROM
import ru.a1024bits.bytheway.util.Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_TO
import ru.a1024bits.bytheway.util.DecimalInputFilter
import java.util.*


/**
 * Created by andrey.gusenkov on 29/09/2017.
 */
class SearchFragment : Fragment(), DatePickerDialog.OnDateSetListener {

    private lateinit var dateDialog: DatePickerDialog

    private fun openDateDialog() {
        dateDialog.setStartTitle("НАЧАЛО")
        dateDialog.setEndTitle("КОНЕЦ")
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

//        dates.put(Constants.START_DATE, getLongFromDate(dayOfMonth, monthOfYear, year))
//        dates.put(Constants.END_DATE, getLongFromDate(dayOfMonthEnd, monthOfYearEnd, yearEnd))

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
                    firstPoint?.let { latLng -> (activity as OnFragmentInteractionListener).onSetPoint(latLng, 1) }
                }
                else -> {
                    val status = PlaceAutocomplete.getStatus(activity, data);
                    Log.i("LOG", status.getStatusMessage() + " ");
                    text_from_city.text = ""
                }
            }

            PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_TO -> when (resultCode) {
                AppCompatActivity.RESULT_OK -> {
                    val place = PlaceAutocomplete.getPlace(activity, data);
                    text_to_city.text = place.name
                    secondPoint = place.latLng
                    secondPoint?.let { latLng -> (activity as OnFragmentInteractionListener).onSetPoint(latLng, 2) }
                }
                else -> {
                    val status = PlaceAutocomplete.getStatus(activity, data);
                    Log.i("LOG", status.statusMessage + " ");
                    text_to_city.text = ""
                }
            }
        }
    }

    var firstPoint: LatLng? = null
    var secondPoint: LatLng? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_search_block, container, false)
        activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        iconCar.setOnClickListener { with(travelCarText) { isActivated = !isActivated } }
        iconTrain.setOnClickListener { with(travelTrainText) { isActivated = !isActivated } }
        iconBus.setOnClickListener { with(travelBusText) { isActivated = !isActivated } }
        iconPlane.setOnClickListener { with(travelPlaneText) { isActivated = !isActivated } }
        iconHitchHicking.setOnClickListener { with(travelHitchHikingText) { isActivated = !isActivated } }

        text_from_city.setOnClickListener {
            sendIntentForSearch(PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_FROM)
        }
        text_to_city.setOnClickListener {
            sendIntentForSearch(PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_TO)
        }

        swap_cities.setOnClickListener {
            val tempString = text_from_city.text
            text_from_city.text = text_to_city.text
            text_to_city.text = tempString
        }

        budgetFromValue.filters = arrayOf(DecimalInputFilter())
        budgetToValue.filters = arrayOf(DecimalInputFilter())
    }

    private fun sendIntentForSearch(code: Int) {
        try {
            val typeFilter = AutocompleteFilter.Builder()
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                    .build()
            val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).setFilter(typeFilter).build(activity)
            startActivityForResult(intent, code)
        } catch (e: GooglePlayServicesRepairableException) {
            e.printStackTrace()
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        }
    }
}