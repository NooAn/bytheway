package ru.a1024bits.bytheway.ui.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
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
    }

    private fun getLongFromDate(day: Int, month: Int, year: Int): Long {
        val dateString = "$day $month $year"
        val dateFormat = SimpleDateFormat("dd MM yyyy")
        val date = dateFormat.parse(dateString)
        val unixTime = date.time.toLong() / 1000
        Log.e("LOG time", unixTime.toString())
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

    private var numberPhone: String = "+7"
    private var whatsAppNumber: String = "+7"
    private var vkLink: String = "https://www.vk.com/"
    private var tgNick = "@"
    private var csLink = "https://www.couchsurfing.com/people/"
    private var fbLink = "https://www.facebook.com/"

    private var methods: HashMap<String, Boolean> = hashMapOf()

    private var socNet: HashMap<String, String> = hashMapOf()

    private var dates: HashMap<String, Long> = hashMapOf()

    private var sex: Int = 0

    private var age: Int = 0

    private var countries: Long = 0

    private var hours: Long = 0

    private var kilometers: Long = 0

    private var cities: HashMap<String, String> = hashMapOf()

    private var budget: Long = 0 // default const

    private var glide: RequestManager? = null

    private var yearNow: Int = 0

    private var yearsArr: ArrayList<Int> = arrayListOf()


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        App.component.inject(this)
        glide = Glide.with(this)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MyProfileViewModel::class.java)

        viewModel?.user?.observe(this, Observer<User> { user ->
            Log.e("LOG", "fill Profile: $user")
            if (user != null) fillProfile(user)
        })

        viewModel?.load(uid)
    }


    private fun fillProfile(user: User) {
        username.text = StringBuilder(user.name).append(" ").append(user.lastName)
        add_info_user.setText(user.addInformation)

        lastName = user.lastName
        name = user.name
        numberPhone = user.phone

        whatsAppNumber = user.socialNetwork.get(SocialNetwork.WHATSAPP.link) ?: whatsAppNumber
        vkLink = user.socialNetwork.get(SocialNetwork.VK.link) ?: vkLink
        fbLink = user.socialNetwork.get(SocialNetwork.FB.link) ?: fbLink
        csLink = user.socialNetwork.get(SocialNetwork.CS.link) ?: csLink
        tgNick = user.socialNetwork.get(SocialNetwork.TG.link) ?: tgNick

        travelledStatistics.visibility = if (user.flightHours == 0L) View.GONE else View.VISIBLE

        travelledCountries.text = user.countries.toString()
        flightHours.text = user.flightHours.toString()
        flightDistance.text = user.kilometers.toString()

        if (user.city.length > 0) {
            cityview.text = user.city
            city = user.city
        }

        if (user.countTrip == 0) {
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
        methods.clear()
        methods.putAll(user.method)
        for (method in user.method.keys) {
            when (method) {
                Method.TRAIN.link -> {
                    if (user.method.get(method) == true)
                        with(iconTrain) { isActivated = true }
                }
                Method.BUS.link -> {
                    if (user.method.get(method) == true)
                        with(iconBus) { isActivated = true }
                }
                Method.CAR.link -> {
                    if (user.method.get(method) == true)
                        with(iconCar) { isActivated = true }
                }
                Method.PLANE.link -> {
                    if (user.method.get(method) == true)
                        with(iconPlane) { isActivated = true }
                }
                Method.HITCHHIKING.link -> {
                    if (user.method.get(method) == true)
                        with(iconHitchHicking) { isActivated = true }
                }
            }
        }


        if (user.budget > 0) {
            budget = user.budget
            displayPriceTravel.text = StringBuilder(getString(R.string.type_money)).append(budget)

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
                    textCityFrom.setText(place.name)
                    cityFromLatLng = GeoPoint(place.latLng.latitude, place.latLng.longitude)
                    cities.put(FIRST_INDEX_CITY, place.name.toString())
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
                    cityToLatLng = GeoPoint(place.latLng.latitude, place.latLng.longitude)
                    cities.put(LAST_INDEX_CITY, place.name.toString())

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
        add_info_user.setText("")
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
        if (googleMap != null)
            this.googleMap = map

        googleMap?.addMarker(MarkerOptions().position(CENTRE).title("Hello, Dude!"))

        // Zooming to the Campus location
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(CENTRE, ZOOM))
    }

    private var googleMap: GoogleMap? = null
    private lateinit var mapView: MapView
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_my_user_profile, container, false)
        mapView = view?.findViewById<MapView>(R.id.mapView)!!
        mapView.onCreate(savedInstanceState)
        mapView.onResume()// needed to get the map to display immediately

        try {
            MapsInitializer.initialize(activity.applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mapView?.getMapAsync(this)
        return view
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
        countTrip = 1
        viewModel?.sendUserData(getHashMapUser(), uid, {
            (activity as MenuActivity).profileChanged = false
        })
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

        Log.e("LOG", "array check! : ${yearsArr.size}")

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

        simpleAlert.setButton(AlertDialog.BUTTON_POSITIVE, "Сохранить", { dialogInterface, i ->
            sex = if (man.isChecked) 1 else if (woman.isChecked) 2 else 0

            //  age = ageChoose.text.toString().toLongOrNull() ?: 0
            fillAgeSex(age, sex)
            name = nameChoose.text.toString()

            lastName = lastNameChoose.text.toString()
            username.text = StringBuilder(name).append(" ").append(lastName)
            city = cityChoose.text.toString()
            cityview.text = if (city.isNotEmpty()) city else "Родной город"
            sendUserInfoToServer()
        })
        simpleAlert.setButton(AlertDialog.BUTTON_NEGATIVE, "Отмена", { dialogInterface, i ->
            simpleAlert.hide()
        })

        simpleAlert.show()
    }


    private fun openDialog(socialNetwork: SocialNetwork) {
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
        simpleAlert.setButton(AlertDialog.BUTTON_NEGATIVE, "Удалить", { dialog, i ->
            viewModel?.error?.observe(this@MyProfileFragment, Observer<Int> { error ->
                when (error) {
                    Constants.ERROR -> {
                        Toast.makeText(this@MyProfileFragment.context, " Ошибка сохранения", Toast.LENGTH_SHORT).show()
                    }
                    Constants.SUCCESS -> {
                        changeSocIconsDisActive(socialNetwork)
                        socNet.remove(socialNetwork.link)
                    }
                }
            })
            viewModel?.saveLinks(socNet, uid)

        })
        simpleAlert.setButton(AlertDialog.BUTTON_POSITIVE, "Сохранить", { dialogInterface, i ->
            val newLink = dialogView.findViewById<EditText>(R.id.socLinkText).text.toString()
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
                budget = fibbonaci(number)
                displayPriceTravel.text = StringBuilder(getString(R.string.type_money)).append(budget)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })

        iconCar.setOnClickListener({
            with(travelCarText) { isActivated = !isActivated }
            methods.put(Method.CAR.link, travelCarText.isActivated)
        })

        iconTrain.setOnClickListener({
            with(travelTrainText) { isActivated = !isActivated }
            methods.put(Method.TRAIN.link, travelTrainText.isActivated)
        })

        iconBus.setOnClickListener({
            with(travelBusText) { isActivated = !isActivated }
            methods.put(Method.BUS.link, travelBusText.isActivated)
        })

        iconPlane.setOnClickListener({
            with(travelPlaneText) { isActivated = !isActivated }
            methods.put(Method.PLANE.link, travelPlaneText.isActivated)
        })

        iconHitchHicking.setOnClickListener({
            with(travelHitchHikingText) { isActivated = !isActivated }
            methods.put(Method.HITCHHIKING.link, travelHitchHikingText.isActivated)
        })


        //view.findViewById<TextView>(R.id.add)
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
            removeTrip()
        }

        mapView.onStart()
    }

    private fun removeTrip() {
        // FixME точно ли нужно удалять. спросить пользователя.
        countTrip = 0
        budget = 0
        methods.clear()
        cities.clear()
        dates.clear()
        getHashMapUser()
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

    companion object {
        private val ARG_PARAM1 = "param1"
        private val UID_KEY = "uid"
        val CENTRE: LatLng = LatLng(-23.570991, -46.649886)
        val ZOOM = 9f

        fun newInstance(param1: String, param2: String): UserProfileFragment {
            val fragment = UserProfileFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            fragment.arguments = args
            return fragment
        }
    }

    private var countTrip: Int = 0

    fun getHashMapUser(): HashMap<String, Any> {
        val hashMap = HashMap<String, Any>()
        hashMap.set("cities", cities)
        hashMap.set("method", methods)
        hashMap.set("dates", dates)
        hashMap.set("budget", budget)
        hashMap.set("cityFromLatLng", cityFromLatLng)
        hashMap.set("cityToLatLng", cityToLatLng)
        hashMap.set("addInformation", add_info_user.text.toString())
        hashMap.set("sex", sex)
        hashMap.set("age", age)
        hashMap.put("countTrip", countTrip)
        return hashMap
    }
}


