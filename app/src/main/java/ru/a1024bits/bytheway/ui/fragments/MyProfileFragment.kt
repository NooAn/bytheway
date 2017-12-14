package ru.a1024bits.bytheway.ui.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.borax12.materialdaterangepicker.date.DatePickerDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
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
import ru.a1024bits.bytheway.model.Method
import ru.a1024bits.bytheway.model.SocialNetwork
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.router.OnFragmentInteractionListener
import ru.a1024bits.bytheway.router.Screens
import ru.a1024bits.bytheway.ui.activity.MenuActivity
import ru.a1024bits.bytheway.util.Constants
import ru.a1024bits.bytheway.util.Constants.END_DATE
import ru.a1024bits.bytheway.util.Constants.FIRST_INDEX_CITY
import ru.a1024bits.bytheway.util.Constants.LAST_INDEX_CITY
import ru.a1024bits.bytheway.util.Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_FROM
import ru.a1024bits.bytheway.util.Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_TO
import ru.a1024bits.bytheway.util.Constants.START_DATE
import ru.a1024bits.bytheway.viewmodel.MyProfileViewModel
import ru.terrakok.cicerone.commands.Replace
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import kotlin.collections.HashMap


class MyProfileFragment : Fragment(), OnMapReadyCallback, DatePickerDialog.OnDateSetListener {
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
        profileStateHashMap.set("dates", dates.toString())
        profileChanged()
    }

    private fun getLongFromDate(day: Int, month: Int, year: Int): Long {
        val dateString = "$day $month $year"
        val dateFormat = SimpleDateFormat("dd MM yyyy")
        val date = dateFormat.parse(dateString)
        val unixTime = date.time.toLong()
        return unixTime
    }

    private var viewModel: MyProfileViewModel? = null


    private var mListener: OnFragmentInteractionListener? = null
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private var uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    private var name = ""
    private var cityFromLatLng = GeoPoint(0.0, 0.0)
    private var cityToLatLng = GeoPoint(0.0, 0.0)

    private var lastName = ""

    private var city = ""

    private lateinit var dateDialog: DatePickerDialog

    val APPNUMBER: String = "+7"
    val VKLINK: String = "https://www.vk.com/"
    val TGLINK = "@"
    val CSLINK = "https://www.couchsurfing.com/people/"
    val FBLINK = "https://www.facebook.com/"

    private var numberPhone: String = "+7"
    private var whatsAppNumber: String = "+7"
    private var vkLink: String = "https://www.vk.com/"
    private var tgNick = "@"
    private var csLink = "https://www.couchsurfing.com/people/"
    private var fbLink = "https://www.facebook.com/"

    private var methods: HashMap<String, Boolean> = hashMapOf(
            Method.CAR.link to false,
            Method.TRAIN.link to false,
            Method.BUS.link to false,
            Method.PLANE.link to false,
            Method.HITCHHIKING.link to false
    )

    private var socNet: HashMap<String, String> = hashMapOf()

    private var dates: HashMap<String, Long> = hashMapOf()

    private var sex: Int = 0

    private var age: Int = 0

    private var countries: Long = 0

    private var hours: Long = 0

    private var kilometers: Long = 0

    private var cities: HashMap<String, String> = hashMapOf()

    private var budget: Long = 0
    private var budgetPosition: Int = 0

    private var glide: RequestManager? = null

    private var yearNow: Int = 0

    private var yearsArr: ArrayList<Int> = arrayListOf()

    private var profileStateHashMap: HashMap<String, String> = hashMapOf()
    private var oldProfileState: Int = 0

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        App.component.inject(this)
        glide = Glide.with(this)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MyProfileViewModel::class.java)

        viewModel?.user?.observe(this, Observer<User> { user ->
            if (user != null) fillProfile(user)
        })

        viewModel?.load(uid)
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

        whatsAppNumber = user.socialNetwork.get(SocialNetwork.WHATSAPP.link) ?: whatsAppNumber
        vkLink = user.socialNetwork.get(SocialNetwork.VK.link) ?: vkLink
        fbLink = user.socialNetwork.get(SocialNetwork.FB.link) ?: fbLink
        csLink = user.socialNetwork.get(SocialNetwork.CS.link) ?: csLink
        tgNick = user.socialNetwork.get(SocialNetwork.TG.link) ?: tgNick
        cityFromLatLng = user.cityFromLatLng
        cityToLatLng = user.cityToLatLng
        travelledStatistics.visibility = if (user.flightHours == 0L) View.GONE else View.VISIBLE

        travelledCountries.text = user.countries.toString()
        flightHours.text = user.flightHours.toString()
        flightDistance.text = user.kilometers.toString()

        if (user.city.length > 0) {
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
            textCityFrom.setText(user.cities.get(FIRST_INDEX_CITY))
            textCityTo.setText(user.cities.get(LAST_INDEX_CITY))
            cities = user.cities
        }

        val formatDate = SimpleDateFormat("dd.MM.yyyy")

        if (user.dates.size > 0) {
            val dayBegin = formatDate.format(Date(user.dates.get(START_DATE) ?: 0))
            val dayArrival = formatDate.format(Date(user.dates.get(END_DATE) ?: 0))
            textDateFrom.setText(dayBegin)
            dateArrived.setText(dayArrival)
            dates = user.dates
        }

        fillAgeSex(user.age, user.sex)

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
        profileStateHashMap.set("methods", methods.hashCode().toString())
        for (method in user.method.keys) {
            when (method) {
                Method.CAR.link -> {
                    if (user.method.get(method) == true) {
                        iconCar.isActivated = true
                    }
                }
                Method.TRAIN.link -> {
                    if (user.method.get(method) == true) {
                        iconTrain.isActivated = true
                    }
                }
                Method.BUS.link -> {
                    if (user.method.get(method) == true) {
                        iconBus.isActivated = true
                    }
                }
                Method.PLANE.link -> {
                    if (user.method.get(method) == true) {
                        iconPlane.isActivated = true
                    }
                }
                Method.HITCHHIKING.link -> {
                    if (user.method.get(method) == true) {
                        iconHitchHicking.isActivated = true
                    }
                }
            }
        }

        if (user.budget > 0) {
            budget = user.budget
            displayPriceTravel.text = StringBuilder(getString(R.string.type_money)).append(budget)
            budgetPosition = user.budgetPosition
            choose_price_travel.setProgress(budgetPosition)
        }

        profileStateHashMap.set("addInformation", user.addInformation)
        saveProfileState()
        add_info_user.setText(user.addInformation)
        add_info_user.clearFocus()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("LOG code:", requestCode.toString() + " " + resultCode + " " + PlaceAutocomplete.getPlace(activity, data))

        // FIXME refactoring in viewModel

        when (requestCode) {
            PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_FROM -> when (resultCode) {
                AppCompatActivity.RESULT_OK -> {
                    val place = PlaceAutocomplete.getPlace(activity, data);
                    textCityFrom.setText(place.name)
                    textCityFrom.error = null
                    cityFromLatLng = GeoPoint(place.latLng.latitude, place.latLng.longitude)
                    cities.put(FIRST_INDEX_CITY, place.name.toString())
                    profileStateHashMap.set("cityFromLatLng", cityFromLatLng.hashCode().toString())
                    profileChanged()
                }
                else -> {
                    val status = PlaceAutocomplete.getStatus(activity, data);
                    Log.i("LOG", status.getStatusMessage() + " ");
                    if (textCityFrom.text.toString().length == 0)
                        textCityFrom.setText("")
                }
            }

            PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_TO -> when (resultCode) {
                AppCompatActivity.RESULT_OK -> {
                    val place = PlaceAutocomplete.getPlace(activity, data);
                    textCityTo.setText(place.name)
                    textCityTo.error = null
                    cityToLatLng = GeoPoint(place.latLng.latitude, place.latLng.longitude)
                    cities.put(LAST_INDEX_CITY, place.name.toString())
                    profileStateHashMap.set("cityToLatLng", cityToLatLng.hashCode().toString())
                    profileChanged()
                }
                else -> {
                    val status = PlaceAutocomplete.getStatus(activity, data);
                    Log.i("LOG", status.statusMessage + " ");
                    if (textCityTo.text.toString().length == 0)
                        textCityTo.setText("")
                }
            }
        }
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
            sex = userSex
            if (userAge > 0) {
                sex_and_age.text = StringBuilder(gender).append(", ").append(userAge)
            } else {
                sex_and_age.text = StringBuilder(gender).append(", Возраст ").append(userAge)
            }
        }

        if (userAge > 0) {
            age = userAge
            if (userSex != 0) {
                sex_and_age.text = StringBuilder(gender).append(", ").append(userAge)
            } else {
                sex_and_age.text = StringBuilder("Пол, ").append(userAge)
            }
        }
    }

    private fun hideBlockTravelInforamtion() {
        direction.visibility = View.GONE
        textCityFrom.setText("")
        textCityTo.setText("")
        dateArrived.setText("")
        textDateFrom.setText("")
        add_info_user.setText("")
        maplayout.visibility = View.GONE
        method_moving.visibility = View.GONE
        with(travelBusText) { isActivated = false }
        with(travelHitchHikingText) { isActivated = false }
        with(travelCarText) { isActivated = false }
        with(travelPlaneText) { isActivated = false }
        with(travelTrainText) { isActivated = false }

        appinTheAirEnter.visibility = View.GONE
        layoutTravelMethod.visibility = View.GONE
        moneyfortrip.visibility = View.GONE
        displayPriceTravel.text = ""
        descriptionprofile.visibility = View.GONE
        button_remove_travel_info.visibility = View.GONE
        button_save_travel_info.visibility = View.GONE
    }

    private fun showBlockAddTrip() {
        add_new_trip.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onMapReady(map: GoogleMap?) {
        this.googleMap = map


        val coordFrom = LatLng(cityFromLatLng.latitude, cityFromLatLng.longitude)
        val coordTo = LatLng(cityToLatLng.latitude, cityToLatLng.longitude)
//          val coordFrom = LatLng(33.981780, -118.236682)
//          val coordTo = LatLng(41.885098, -87.630201)
//          routes = "a~l~Fjk~uOnzh@vlbBtc~@tsE`vnApw{A`dw@~w\\|tNtqf@l{Yd_Fblh@rxo@b}@xxSfytAblk@xxaBeJxlcBb~t@zbh@jc|Bx}C`rv@rw|@rlhA~dVzeo@vrSnc}Axf]fjz@xfFbw~@dz{A~d{A|zOxbrBbdUvpo@`cFp~xBc`Hk@nurDznmFfwMbwz@bbl@lq~@loPpxq@bw_@v|{CbtY~jGqeMb{iF|n\\~mbDzeVh_Wr|Efc\\x`Ij{kE}mAb~uF{cNd}xBjp]fulBiwJpgg@|kHntyArpb@bijCk_Kv~eGyqTj_|@`uV`k|DcsNdwxAott@r}q@_gc@nu`CnvHx`k@dse@j|p@zpiAp|gEicy@`omFvaErfo@igQxnlApqGze~AsyRzrjAb__@ftyB}pIlo_BflmA~yQftNboWzoAlzp@mz`@|}_@fda@jakEitAn{fB_a]lexClshBtmqAdmY_hLxiZd~XtaBndgC"


        val midPointLat = (coordFrom.latitude + coordTo.latitude) / 2
        val midPointLong = (coordFrom.longitude + coordTo.longitude) / 2
        val blueMarker = BitmapDescriptorFactory.fromResource(R.drawable.pin_blue)
        val blueColor = -0x657db
        googleMap?.addMarker(MarkerOptions()
                .icon(blueMarker)
                .position(coordFrom)
                .title("First Point"))
        googleMap?.addMarker(MarkerOptions()
                .icon(blueMarker)
                .position(coordTo)
                .title("Final Point"))

        val options = PolylineOptions()
        options.color(blueColor)
        options.width(5f)


        if (routes != "") {
            var polyPts: List<LatLng>
            polyPts = PolyUtil.decode(routes)

            for (pts in polyPts) {
                options.add(pts)
            }
            googleMap?.addPolyline(options)
        }

        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(midPointLat, midPointLong), 3.0f))
    }

    private var routes: String = ""

    private var googleMap: GoogleMap? = null
    private lateinit var mapView: MapView
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_my_user_profile, container, false)
        mapView = view?.findViewById<MapView>(R.id.mapView)!!

        try {
            mapView.onCreate(savedInstanceState)
            mapView.onResume()// needed to get the map to display immediately
            MapsInitializer.initialize(activity.applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mapView?.getMapAsync(this)

        return view
    }

    fun TextView.afterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(editable: Editable?) {
                afterTextChanged.invoke(editable.toString())
            }
        })
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

    private fun openDateDialog() {
        dateDialog.setStartTitle("НАЧАЛО")
        dateDialog.setEndTitle("КОНЕЦ")
        dateDialog.accentColor = resources.getColor(R.color.colorPrimary)
        dateDialog.show(activity.fragmentManager, "")
    }

    private fun sendUserInfoToServer() {
        var isEmpty = textCityFrom.text.isEmpty() || textCityTo.text.isEmpty()

        if (isEmpty) {
            var toastString = getString(R.string.fill_required_fields)
            if (textCityFrom.text.isEmpty()) {
                textCityFrom.error = getString(R.string.name)
                toastString += " " + getString(R.string.city_from)
            } else {
                textCityFrom.error = null
            }
            if (textCityTo.text.isEmpty()) {
                textCityTo.error = getString(R.string.yes)
                toastString += " " + getString(R.string.city_to)
            } else {
                textCityTo.error = null
            }
            textCityFrom.getParent().requestChildFocus(textCityFrom, textCityFrom)
            Toast.makeText(this@MyProfileFragment.context, toastString, Toast.LENGTH_LONG).show();

            return
        }
        countTrip = 1
        viewModel?.sendUserData(getHashMapUser(), uid, {
            Toast.makeText(this@MyProfileFragment.context, resources.getString(R.string.save_succesfull), Toast.LENGTH_SHORT).show()
            profileChanged(false)
        })
    }

    fun validCellPhone(number: String): Boolean {
        return number.matches(Regex("^\\+[0-9]{10,13}\$"))
    }

    private fun showBlockTravelInformation() {
        direction.visibility = View.VISIBLE
        maplayout.visibility = View.VISIBLE
        method_moving.visibility = View.VISIBLE
        layoutTravelMethod.visibility = View.VISIBLE
        moneyfortrip.visibility = View.VISIBLE
        appinTheAirEnter.visibility = View.VISIBLE
        descriptionprofile.visibility = View.VISIBLE
        button_remove_travel_info.visibility = View.VISIBLE
        button_save_travel_info.visibility = View.VISIBLE
    }

    private fun hideBlockNewTrip() {
        add_new_trip.visibility = View.GONE
    }

    private fun openInformationEditDialog() {
        val simpleAlert = AlertDialog.Builder(activity).create()
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.custom_dialog_profile_inforamtion, null)

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

        for (i in 1920..yearNow) {
            yearsArr.add(i)
        }

        val yearsAdapter = ArrayAdapter<Int>(this.context, android.R.layout.simple_spinner_item, yearsArr);

        yearsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerYearsView.adapter = yearsAdapter
        spinnerYearsView.prompt = "Дата"

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

        simpleAlert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.save), { dialogInterface, i ->
            sex = if (man.isChecked) 1 else if (woman.isChecked) 2 else 0
            fillAgeSex(age, sex)
            name = nameChoose.text.toString().capitalize()
            lastName = lastNameChoose.text.toString().capitalize()
            username.text = StringBuilder(name).append(" ").append(lastName)
            city = cityChoose.text.toString().capitalize()
            cityview.text = if (city.isNotEmpty()) city else getString(R.string.native_city)
            viewModel?.sendUserData(getHashMapUser(), uid)
        })
        simpleAlert.setButton(AlertDialog.BUTTON_NEGATIVE, "Отмена", { dialogInterface, i ->
            simpleAlert.hide()
        })

        simpleAlert.show()
    }


    private fun openDialog(socialNetwork: SocialNetwork, errorText: String? = null) {
        val simpleAlert = AlertDialog.Builder(activity).create()
        simpleAlert.setTitle("Ссылки на социальные сети")
        simpleAlert.setMessage("Здесь вы можете указать ваши контактные данные для того что бы вас смогли найти другие путешественики")
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.custom_dialog_profile_soc_network, null)
        simpleAlert.setView(dialogView)
        dialogView.findViewById<EditText>(R.id.socLinkText).setText(
                when (socialNetwork) {
                    SocialNetwork.VK -> vkLink
                    SocialNetwork.WHATSAPP -> whatsAppNumber
                    SocialNetwork.CS -> csLink
                    SocialNetwork.FB -> fbLink
                    SocialNetwork.TG -> (if (tgNick.length > 1) tgNick else numberPhone)
                })
        if (errorText != null) {
            dialogView.findViewById<EditText>(R.id.socLinkText).error = errorText
        }
        simpleAlert.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.remove), { dialog, i ->
            viewModel?.error?.observe(this@MyProfileFragment, Observer<Int> { error ->
                socNet.remove(socialNetwork.link)
                when (error) {
                    Constants.ERROR -> {
                        Toast.makeText(this@MyProfileFragment.context, " Ошибка обновления", Toast.LENGTH_SHORT).show()
                    }
                    Constants.SUCCESS -> {
                        changeSocIconsDisActive(socialNetwork)
                        when (socialNetwork) {
                            SocialNetwork.VK -> vkLink = VKLINK
                            SocialNetwork.WHATSAPP -> whatsAppNumber = APPNUMBER
                            SocialNetwork.CS -> csLink = CSLINK
                            SocialNetwork.FB -> fbLink = FBLINK
                            SocialNetwork.TG -> tgNick = TGLINK
                        }
                    }
                }
            })
            viewModel?.saveLinks(socNet, uid)

        })
        simpleAlert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.save), { dialogInterface, i ->
            val newLink = dialogView.findViewById<EditText>(R.id.socLinkText).text.toString()
            var valid = true
            var errorText = ""
            if (socialNetwork == SocialNetwork.WHATSAPP && !validCellPhone(newLink)) {
                valid = false
                errorText = getString(R.string.fill_phone_invalid)
            }
            if (valid) {
                socNet.put(socialNetwork.link, newLink)
                viewModel?.error?.observe(this@MyProfileFragment, Observer<Int> { error ->
                    when (error) {
                        Constants.ERROR -> {
                            Toast.makeText(this@MyProfileFragment.context, " Ошибка сохранения", Toast.LENGTH_SHORT).show()
                        }
                        Constants.SUCCESS -> {
                            changeSocIconsActive(socialNetwork)
                            when (socialNetwork) {
                                SocialNetwork.VK -> vkLink = newLink
                                SocialNetwork.WHATSAPP -> whatsAppNumber = newLink
                                SocialNetwork.CS -> csLink = newLink
                                SocialNetwork.FB -> fbLink = newLink
                                SocialNetwork.TG -> tgNick = newLink
                            }
                        }
                    }
                })
                viewModel?.saveLinks(socNet, uid)
            } else {
                openDialog(socialNetwork, errorText)
            }
        })
        simpleAlert.show()
    }

    private fun openAlertDialog(callback: () -> Unit) {
        val simpleAlert = AlertDialog.Builder(activity).create()
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.confirm_dialog, null)
        simpleAlert.setView(dialogView)
        dialogView.textMessage.text = getString(R.string.text_confirm_remove_trip)
        simpleAlert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.yes), { dialogInterface, i ->
            callback()
        })
        simpleAlert.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.no), { dialogInterface, i ->
        })
        simpleAlert.show()
    }

    private fun changeSocIconsActive(socialNetwork: SocialNetwork) {
        when (socialNetwork) {
            SocialNetwork.VK -> vkIcon.setImageResource(R.drawable.ic_vk_color)
            SocialNetwork.CS -> csIcon.setImageResource(R.drawable.ic_cs_color)
            SocialNetwork.FB -> fbcon.setImageResource(R.drawable.ic_fb_color)
            SocialNetwork.WHATSAPP -> whatsAppIcon.setImageResource(R.drawable.ic_whats_icon_color)
            SocialNetwork.TG -> tgIcon.setImageResource(R.drawable.ic_tg_color)

        }
    }

    private fun changeSocIconsDisActive(socialNetwork: SocialNetwork) {
        when (socialNetwork) {
            SocialNetwork.VK -> vkIcon.setImageResource(R.drawable.ic_vk_gray)
            SocialNetwork.CS -> csIcon.setImageResource(R.drawable.ic_cs_grey)
            SocialNetwork.FB -> fbcon.setImageResource(R.drawable.ic_fb_grey)
            SocialNetwork.WHATSAPP -> whatsAppIcon.setImageResource(R.drawable.ic_whats_icon_grey)
            SocialNetwork.TG -> tgIcon.setImageResource(R.drawable.tg_grey)
        }
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
        mapView.onSaveInstanceState(outState)
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

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

        headerprofile.setOnClickListener({
            openInformationEditDialog()
        })

        choose_price_travel.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, number: Int, p2: Boolean) {
                budget = (150 * number).toLong()// = fibbonaci(number)
                displayPriceTravel.text = StringBuilder(getString(R.string.type_money)).append(budget)
                if (number != budgetPosition) {
                    budgetPosition = number
                    profileStateHashMap.set("budget", budget.toString())
                    profileStateHashMap.set("budgetPosition", number.toString())
                    profileChanged()
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })

        iconCar.setOnClickListener({
            with(travelCarText) { isActivated = !isActivated }
            methods.put(Method.CAR.link, travelCarText.isActivated)
            profileStateHashMap.set("methods", methods.hashCode().toString())
            profileChanged()
        })

        iconTrain.setOnClickListener({
            with(travelTrainText) { isActivated = !isActivated }
            methods.put(Method.TRAIN.link, travelTrainText.isActivated)
            profileStateHashMap.set("methods", methods.hashCode().toString())
            profileChanged()
        })

        iconBus.setOnClickListener({
            with(travelBusText) { isActivated = !isActivated }
            methods.put(Method.BUS.link, travelBusText.isActivated)
            profileStateHashMap.set("methods", methods.hashCode().toString())
            profileChanged()
        })

        iconPlane.setOnClickListener({
            with(travelPlaneText) { isActivated = !isActivated }
            methods.put(Method.PLANE.link, travelPlaneText.isActivated)
            profileStateHashMap.set("methods", methods.hashCode().toString())
            profileChanged()
        })

        iconHitchHicking.setOnClickListener({
            with(travelHitchHikingText) { isActivated = !isActivated }
            methods.put(Method.HITCHHIKING.link, travelHitchHikingText.isActivated)
            profileStateHashMap.set("methods", methods.hashCode().toString())
            profileChanged()
        })

        new_trip_text.setOnClickListener {
            hideBlockNewTrip()
            showBlockTravelInformation()
        }
        vkIcon.setOnClickListener {
            /*
             Если контакты еще не добавлены, тогда открываем диалоговое окно.
             Если были какие-то изменения в линках то сохраняем в бд. И меняем цвет иконки соответсвенно значениям.
             */
            openDialog(SocialNetwork.VK)
        }
        csIcon.setOnClickListener() {
            openDialog(SocialNetwork.CS)
        }
        whatsAppIcon.setOnClickListener({
            openDialog(SocialNetwork.WHATSAPP)
        })
        fbcon.setOnClickListener({
            openDialog(SocialNetwork.FB)
        })
        tgIcon.setOnClickListener({
            openDialog(SocialNetwork.TG)
        })

        button_save_travel_info.setOnClickListener({
            Log.e("LOG", "save travel")
            sendUserInfoToServer()
        })
        dateArrived.setOnClickListener {
            openDateDialog()
        }
        textDateFrom.setOnClickListener {
            openDateDialog()
        }
        add_info_user.afterTextChanged({
            profileStateHashMap.set("addInformation", it)
            profileChanged()
        })
        textCityFrom.setOnClickListener {
            sendIntentForSearch(PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_FROM)
        }

        textCityTo.setOnClickListener {
            sendIntentForSearch(PLACE_AUTOCOMPLETE_REQUEST_CODE_TEXT_TO)
        }
        appinTheAirEnter.setOnClickListener {
            (activity as MenuActivity).navigator.applyCommand(Replace(Screens.USER_SINHRONIZED_SCREEN, 1))
        }
        button_remove_travel_info.setOnClickListener {
            openAlertDialog(this::removeTrip)
        }

        mapView.onStart()
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

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    fun fibbonaci(n: Int): Long {
        var prev: Long = 0
        var next: Long = 1
        var result: Long = 0
        for (i in 0 until n) {
            result = prev + next
            prev = next
            next = result
        }
        return result
    }

    private var countTrip: Int = 0

    fun getHashMapUser(): HashMap<String, Any> {
        val hashMap = HashMap<String, Any>()
        hashMap.set("cities", cities)
        hashMap.set("method", methods)
        hashMap.set("dates", dates)
        hashMap.set("budget", budget)
        hashMap.set("budgetPosition", budgetPosition)
        hashMap.set("cityFromLatLng", cityFromLatLng)
        hashMap.set("cityToLatLng", cityToLatLng)
        hashMap.set("addInformation", add_info_user.text.toString())
        hashMap.put("countTrip", countTrip)
        hashMap.set("sex", sex)
        hashMap.set("age", age)
        hashMap.set("name", name)
        hashMap.set("lastName", lastName)
        hashMap.set("city", city)
        return hashMap
    }

    fun getHashMapInfoUser(): HashMap<String, Any> {
        val hashMap = HashMap<String, Any>()

        return hashMap
    }

    fun saveProfileState() {
        profileStateHashMap.set("dates", dates.toString())
        profileStateHashMap.set("budget", budget.toString())
        profileStateHashMap.set("budgetPosition", budgetPosition.toString())
        profileStateHashMap.set("cityFromLatLng", cityFromLatLng.hashCode().toString())
        profileStateHashMap.set("cityToLatLng", cityToLatLng.hashCode().toString())
        oldProfileState = profileStateHashMap.hashCode()
    }

    fun profileChanged(force: Boolean? = null) {
        if (countTrip == 1) {
            val changed: Boolean = if (force != null) force
            else profileStateHashMap.hashCode() != oldProfileState

            (activity as MenuActivity).profileChanged = changed
        }
    }
}


