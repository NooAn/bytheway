package ru.a1024bits.bytheway.ui.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_my_user_profile.*
import kotlinx.android.synthetic.main.profile_direction.*
import kotlinx.android.synthetic.main.profile_main_image.*
import ru.a1024bits.bytheway.App
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.model.Method
import ru.a1024bits.bytheway.model.SocialNetwork
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.router.OnFragmentInteractionListener
import ru.a1024bits.bytheway.viewmodel.MyProfileViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import kotlin.collections.HashMap


class MyProfileFragment : Fragment(), OnMapReadyCallback {

    private var viewModel: MyProfileViewModel? = null


    private var mListener: OnFragmentInteractionListener? = null
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private var uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    private var name = ""

    private var lastName = ""

    private var city = ""

    private var methods: ArrayList<Method> = arrayListOf()

    private var socNet: ArrayList<SocialNetwork> = arrayListOf()

    private var dates: ArrayList<Long> = arrayListOf()

    private var sex: Int = 0

    private var age: Long = 0

    private var countries: Long = 0

    private var hours: Long = 0

    private var kilometers: Long = 0

    private var cities: ArrayList<String> = arrayListOf()

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

        viewModel!!.load(uid)
    }


    private fun fillProfile(user: User) {
        username.text = StringBuilder(user.name).append(" ").append(user.lastName)

        lastName = user.lastName
        name = user.name

        if (user.flightHours == 0L) {
            travelledStatistics.visibility = View.GONE
        } else {
            travelledStatistics.visibility = View.VISIBLE
        }

        travelledCountries.text = user.countries.toString()
        flightHours.text = user.flightHours.toString()
        flightDistance.text = user.kilometers.toString()

        if (user.city.length > 0) {
            cityview.text = user.city
            city = user.city
        }
        Log.e("LOGER", "" + user.countTrip)

        if (user.countTrip == 0) {
            showBlockAddTrip()
            hideBlockTravelInforamtion()
        } else {
            hideBlockNewTrip()
            showBlockTravelInformation()
        }

        if (user.cities.size > 0) {
            val lastIndexArr = user.cities.size - 1
            textCityFrom.setText(user.cities.get(0))
            textCityTo.setText(user.cities.get(lastIndexArr))
            cities.add(user.cities.get(0))
            cities.add(user.cities.get(lastIndexArr))
        }

        val formatDate = SimpleDateFormat("dd.MM.yyyy")

        if (user.dates.size > 0) {
            val lastIndexArr = user.dates.size - 1
            val dayBegin = formatDate.format(Date(user.dates.get(0)))
            val dayArrival = formatDate.format(Date(user.dates.get(lastIndexArr)))
            textDateFrom.setText(dayBegin)
            dateArrived.setText(dayArrival)

            dates.add(user.dates.get(0))
            dates.add(user.dates.get(lastIndexArr))
        }

        fillAgeSex(user.age, user.sex)
        age = user.age

        glide?.load(user.urlPhoto)
                ?.apply(RequestOptions.circleCropTransform())
                ?.into(image_avatar)

        for (name in user.socialNetwork) {
            socNet.add(name)
            when (name) {
                SocialNetwork.VK -> vkIcon.setImageResource(R.drawable.ic_vk_color)
                SocialNetwork.CS -> csIcon.setImageResource(R.drawable.ic_cs_color)
                SocialNetwork.FB -> fbcon.setImageResource(R.drawable.ic_fb_color)
                SocialNetwork.WHATSAAP -> whatsUpIcon.setImageResource(R.drawable.ic_whats_icon_color)
                SocialNetwork.TG -> tgIcon.setImageResource(R.drawable.ic_tg_color)

            }
        }
        for (method in user.method) {
            when (method) {
                Method.TRAIN -> {
                    directions_railway.setImageResource(R.drawable.ic_directions_railway)
                    methods.add(Method.TRAIN)
                }
                Method.BUS -> {
                    directions_bus.setImageResource(R.drawable.ic_directions_bus)
                    methods.add(Method.BUS)
                }
                Method.CAR -> {
                    directions_car.setImageResource(R.drawable.ic_directions_car)
                    methods.add(Method.CAR)
                }
                Method.PLANE -> {
                    directions_flight.setImageResource(R.drawable.ic_flight)
                    methods.add(Method.PLANE)
                }
                Method.HITCHHIKING -> {
                    csIcon1.setImageResource(R.drawable.ic_directions_hitchhiking)
                    methods.add(Method.HITCHHIKING)
                }
                else -> {
                }
            }
        }


        if (user.budget > 0) {
            budget = user.budget
        }
    }

    fun fillAgeSex(userAge: Long, userSex: Int) {
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
                sex_and_age.text = gender
            }
        }

        if (userAge > 0) {
            age = userAge
            if (userSex != 0) {
                sex_and_age.text = StringBuilder(gender).append(", ").append(userAge)
            } else {
                sex_and_age.text = userAge.toString()
            }
        }
    }

    private fun hideBlockTravelInforamtion() {
        direction.visibility = View.GONE
        maplayout.visibility = View.GONE
        method_moving.visibility = View.GONE
        layout_method_moving.visibility = View.GONE
        moneyfortrip.visibility = View.GONE
        descriptionprofile.visibility = View.GONE
        button_remove_travel_info.visibility = View.GONE
        button_save_travel_info.visibility = View.GONE
    }

    private fun showBlockAddTrip() {
        add_new_trip.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        mMapView?.onResume()
    }

    override fun onMapReady(map: GoogleMap?) {
        if (googleMap != null)
            this.googleMap = map

        googleMap?.addMarker(MarkerOptions().position(CENTRE).title("Hello, Dude!"))

        // Zooming to the Campus location
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(CENTRE, ZOOM))
    }

    private var mMapView: MapView? = null
    private var googleMap: GoogleMap? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_my_user_profile, container, false)

        val displayPriceTravel = view.findViewById<TextView>(R.id.display_price_travel)
        displayPriceTravel.text = StringBuilder(getString(R.string.type_money)).append(budget)

        view.findViewById<LinearLayout>(R.id.headerprofile).setOnClickListener({
            openInformationEditDialog()
        })

        view.findViewById<SeekBar>(R.id.choose_price_travel).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, number: Int, p2: Boolean) {
                budget = fibbonaci(number)
                displayPriceTravel.text = StringBuilder(getString(R.string.type_money)).append(budget)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })

        view.findViewById<ImageView>(R.id.directions_car).setOnClickListener({

            if (Method.CAR in methods){


                directions_car.setImageResource(R.drawable.ic_directions_car_grey)
                methods.remove(Method.CAR)
            } else {
                directions_car.setImageResource(R.drawable.ic_directions_car)
                methods.add(Method.CAR)
            }


        })

        view.findViewById<ImageView>(R.id.directions_railway).setOnClickListener({
            if (Method.TRAIN in methods) {


                directions_railway.setImageResource(R.drawable.ic_directions_railway_grey)
                methods.remove(Method.TRAIN)
            } else {
                directions_railway.setImageResource(R.drawable.ic_directions_railway)
                methods.add(Method.TRAIN)
            }
        })

        view.findViewById<ImageView>(R.id.directions_bus).setOnClickListener({

            if (Method.BUS in methods) {

                directions_bus.setImageResource(R.drawable.ic_directions_bus_grey)
                methods.remove(Method.BUS)
            } else {
                directions_bus.setImageResource(R.drawable.ic_directions_bus)
                methods.add(Method.BUS)
            }
        })

        view.findViewById<ImageView>(R.id.directions_flight).setOnClickListener({

            if (Method.PLANE in methods) {

                directions_flight.setImageResource(R.drawable.ic_flight_grey)
                methods.remove(Method.PLANE)
            } else {
                directions_flight.setImageResource(R.drawable.ic_flight)
                methods.add(Method.PLANE)
            }
        })

        view.findViewById<ImageView>(R.id.csIcon1).setOnClickListener({

            if (Method.HITCHHIKING in methods) {

                csIcon1.setImageResource(R.drawable.ic_directions_hitchhiking)
                methods.remove(Method.HITCHHIKING)
            } else {
                csIcon1.setImageResource(R.drawable.ic_directions_hitchhiking_grey)
                methods.add(Method.HITCHHIKING)
            }
        })


        view.findViewById<TextView>(R.id.add)
        view.findViewById<TextView>(R.id.new_trip_text).setOnClickListener {
            hideBlockNewTrip()
            showBlockTravelInformation()
        }
        view.findViewById<ImageView>(R.id.vkIcon).setOnClickListener {
            openDialog(SocialNetwork.VK)
            vkIcon.setImageResource(R.drawable.ic_vk_color)

        }
        view.findViewById<ImageView>(R.id.csIcon).setOnClickListener {
            openDialog(SocialNetwork.CS)
            csIcon.setImageResource(R.drawable.ic_cs_color)
        }
        view.findViewById<ImageView>(R.id.whatsUpIcon).setOnClickListener({
            openDialog(SocialNetwork.WHATSAAP)
            whatsUpIcon.setImageResource(R.drawable.ic_whats_icon_color)
        })
        view.findViewById<ImageView>(R.id.fbcon).setOnClickListener({
            openDialog(SocialNetwork.FB)
            fbcon.setImageResource(R.drawable.ic_fb_color)
        })
        view.findViewById<ImageView>(R.id.tgIcon).setOnClickListener({
            openDialog(SocialNetwork.TG)
            tgIcon.setImageResource(R.drawable.ic_tg_color)
        })
        view.findViewById<Button>(R.id.button_save_travel_info).setOnClickListener({
            Log.e("LOG", "save travel")
            sendUserInfoToServer()
        })

        mMapView = view?.findViewById<MapView>(R.id.mapView)
        mMapView?.onCreate(savedInstanceState)
        mMapView?.onResume()// needed to get the map to display immediately

        try {
            MapsInitializer.initialize(activity.applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mMapView?.getMapAsync(this)
        return view
    }

    private fun sendUserInfoToServer() {
        viewModel?.sendUserData(getHashMapUser(), uid)
    }

    private fun showBlockTravelInformation() {
        direction.visibility = View.VISIBLE
        maplayout.visibility = View.VISIBLE
        method_moving.visibility = View.VISIBLE
        layout_method_moving.visibility = View.VISIBLE
        moneyfortrip.visibility = View.VISIBLE
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

        spinnerYearsView.setSelection(1)
        spinnerYearsView.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString().toLong()
                age = (yearNow - selectedItem)
                Log.e("LOG", "Click 2")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                Log.e("LOG", "Nothing 2")
            }
        }

        simpleAlert.setButton(AlertDialog.BUTTON_POSITIVE, "Сохранить", { dialogInterface, i ->
            if (man.isChecked) {
                sex = 1
            } else if (woman.isChecked) {
                sex = 2
            } else sex = 0

            fillAgeSex(age, sex)
            name = nameChoose.text.toString()

            lastName = lastNameChoose.text.toString()
            username.text = StringBuilder(name).append(" ").append(lastName)
            city = cityChoose.text.toString()
            cityview.text = (city)
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
        simpleAlert.setButton(AlertDialog.BUTTON_POSITIVE, "Сохранить", { dialogInterface, i ->
            //  socNet.add(socialNetwork.apply { link = dialogView.findViewById<EditText>(R.id.socLinkText).text.toString() })
            socialNetwork.link = dialogView.findViewById<EditText>(R.id.socLinkText).text.toString()
            socNet.add(socialNetwork)
            viewModel?.saveLinks(socNet, uid)
        })
        simpleAlert.show()
    }


    fun onButtonPressed() {
        mListener?.onFragmentInteraction()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
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


    fun getHashMapUser(): HashMap<String, Any> {
        val hashMap = HashMap<String, Any>()
        hashMap.set("name", name)
        hashMap.set("lastName", lastName)
        hashMap.set("city", city)
        hashMap.set("cities", cities)
        //  hashMap.set("method", methods)
        // hashMap.set("socNet", socNet)
        hashMap.set("dates", dates)
        hashMap.set("budget", budget)
        hashMap.set("addInformation", add_info_user.text.toString())
        hashMap.set("sex", sex)
        hashMap.set("age", age)
        hashMap.put("countTrip", 1)
        return hashMap
    }
}


