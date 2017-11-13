package ru.a1024bits.bytheway.ui.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.text.Layout
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
import javax.inject.Inject
import java.util.Date
import java.text.SimpleDateFormat


class MyProfileFragment : Fragment(), OnMapReadyCallback {
    private var viewModel: MyProfileViewModel? = null
    private var mListener: OnFragmentInteractionListener? = null
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    private var glide: RequestManager? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        App.component.inject(this)
        glide = Glide.with(this)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MyProfileViewModel::class.java)

        viewModel?.user?.observe(this, Observer<User> { user ->
            Log.e("LOG", "fill Profile: $user")
            if (user != null) fillProfile(user)
        })

        viewModel!!.load(FirebaseAuth.getInstance().currentUser?.uid.orEmpty())

    }


    private fun fillProfile(user: User) {
        username.text = StringBuilder(user.name).append(" ").append(user.lastName)
        if (user.city.length > 0) city.text = user.city
        if (user.countTrip == 0) {
            showBlockAddTrip()
            hideBlockTravelInforamtion()
        }
        if (user.cities.size > 0) {
            val lastIndexArr = user.cities.size - 1
            textCityFrom.text = user.cities.get(0)
            textCityTo.text = user.cities.get(lastIndexArr)
            cities.add(user.cities.get(0))
            cities.add(user.cities.get(lastIndexArr))

        }

        val formatDate = SimpleDateFormat("dd.MM.yyyy")

        if (user.dates.size > 0) {
            val lastIndexArr = user.dates.size - 1
            val dayBegin = formatDate.format(Date(user.dates.get(0)))
            val dayArrival = formatDate.format(Date(user.dates.get(lastIndexArr)))
            textDateFrom.text = dayBegin
            textView5.text = dayArrival
            dates.add(user.dates.get(0))
            dates.add(user.dates.get(lastIndexArr))

        }

        glide?.load(user.urlPhoto)
                ?.apply(RequestOptions.circleCropTransform())
                ?.into(image_avatar)


        for (name in user.socialNetwork) {

            when (name) {
                SocialNetwork.VK -> vkIcon.setImageResource(R.drawable.vk)
                SocialNetwork.CS -> csIcon.setImageResource(R.drawable.cs_color)
                SocialNetwork.FB -> fbcon.setImageResource(R.drawable.fb_color)
                SocialNetwork.WHATSAAP -> whatsUpIcon.setImageResource(R.drawable.whats_icon__2_)
                SocialNetwork.TG -> tgIcon.setImageResource(R.drawable.tg_color)

            }
        }
        for (method in user.method) {
            when (method) {
                Method.TRAIN -> {directions_railway.setImageResource(R.drawable.ic_directions_railway)
                    methods.add(Method.TRAIN)}
                Method.BUS -> {directions_bus.setImageResource(R.drawable.ic_directions_bus)
                    methods.add(Method.BUS)}
                Method.CAR -> {directions_car.setImageResource(R.drawable.ic_directions_car)
                    methods.add(Method.CAR)}
                Method.PLANE -> {directions_flight.setImageResource(R.drawable.ic_flight)
                    methods.add(Method.PLANE)}
                Method.HITCHHIKING -> {csIcon1.setImageResource(R.drawable.ic_directions_hitchhiking)
                    methods.add(Method.HITCHHIKING)}
                else -> {
                }
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
    private var budget: Int = -1 // default const
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_my_user_profile, container, false)
        Log.e("LOG", "function activity create view ")

        val displayPriceTravel = view.findViewById<TextView>(R.id.display_price_travel)
        displayPriceTravel.text = StringBuilder(getString(R.string.type_money)).append(0)

        view.findViewById<LinearLayout>(R.id.headerprofile).setOnClickListener({
            openSettingDialog()
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

        /*
        this code copy for others icon_method
         */
        view.findViewById<ImageView>(R.id.directions_car).setOnClickListener({

            if (checkInMethods(Method.CAR)){
                directions_car.setImageResource(R.drawable.ic_directions_car_grey)
                methods.remove(Method.CAR)
            }else {
                directions_car.setImageResource(R.drawable.ic_directions_car)
                methods.add(Method.CAR)
            }

            // directions_car.setImageDrawable(resources(R.drawable.ic_directions_car)) // put only gray color!
            // do change icons and add method ( or delete)  in @methods

            // Если пользователь выбрал машину то надо добавить, если он повторно нажал на кнопку то удаляем.
            // И в случае когда у нас уже есть Машина с сервера, то тоже удаляем.
            // изменить условие

          //  if (true) methods.add(Method.CAR)
          //  else methods.remove(Method.CAR)
        })

        view.findViewById<ImageView>(R.id.directions_railway).setOnClickListener({
            if (checkInMethods(Method.TRAIN)){
                directions_railway.setImageResource(R.drawable.ic_directions_railway_grey)
                methods.remove(Method.TRAIN)
            }else {
                directions_railway.setImageResource(R.drawable.ic_directions_railway)
                methods.add(Method.TRAIN)
            }
        })

        view.findViewById<ImageView>(R.id.directions_bus).setOnClickListener({
            if (checkInMethods(Method.BUS)){
                directions_bus.setImageResource(R.drawable.ic_directions_bus_grey)
                methods.remove(Method.BUS)
            }else {
                directions_bus.setImageResource(R.drawable.ic_directions_bus)
                methods.add(Method.BUS)
            }
        })

        view.findViewById<ImageView>(R.id.directions_flight).setOnClickListener({
            if (checkInMethods(Method.PLANE)){
                directions_flight.setImageResource(R.drawable.ic_flight_grey)
                methods.remove(Method.PLANE)
            }else {
                directions_flight.setImageResource(R.drawable.ic_flight)
                methods.add(Method.PLANE)
            }
        })

        view.findViewById<ImageView>(R.id.csIcon1).setOnClickListener({
            if (checkInMethods(Method.HITCHHIKING)){
                csIcon1.setImageResource(R.drawable.ic_directions_hitchhiking)
                methods.remove(Method.HITCHHIKING)
            }else {
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
        }
        view.findViewById<ImageView>(R.id.csIcon).setOnClickListener {
            openDialog(SocialNetwork.CS)
        }
        view.findViewById<ImageView>(R.id.whatsUpIcon).setOnClickListener({
            openDialog(SocialNetwork.WHATSAAP)
        })
        view.findViewById<ImageView>(R.id.fbcon).setOnClickListener({
            openDialog(SocialNetwork.FB)
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
        viewModel?.sendUserData(getHashMapUser(), FirebaseAuth.getInstance().currentUser?.uid!!)
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

    private fun openSettingDialog() {
        val simpleAlert = AlertDialog.Builder(activity).create()
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.custom_dialog_profile_inforamtion, null)
        simpleAlert.setView(dialogView)
        simpleAlert.setButton(AlertDialog.BUTTON_POSITIVE, "Сохранить", { dialogInterface, i ->

        })
        simpleAlert.setButton(AlertDialog.BUTTON_NEGATIVE, "Отмена", { dialogInterface, i ->

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
            viewModel?.saveLinks(dialogView.findViewById<EditText>(R.id.socLinkText).text, socialNetwork)
        })
        simpleAlert.show()
    }


    fun onButtonPressed() {
        if (mListener != null) {
            mListener!!.onFragmentInteraction()
        }
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

    fun fibbonaci(n: Int): Int {
        var prev = 0
        var next = 1
        var result = 0
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

    private var methods: ArrayList<Method> = arrayListOf()

    fun checkInMethods( method: Method):Boolean{

       var result=false
        for (item in methods) {
            if (method == item) result = true
        }
        return result
    }

    private var dates: ArrayList<Long> = arrayListOf()

    private var sex: Int = -1

    private var cities: ArrayList<String> = arrayListOf()

    fun getHashMapUser(): HashMap<String, Any> {
        val hashMap = HashMap<String, Any>()

        hashMap.set("cities", cities)
        hashMap.set("method", methods)
        hashMap.set("dates", dates)
        hashMap.set("budget", budget)
        hashMap.set("addInformation", add_info_user.text.toString())
        hashMap.set("sex", sex)
        hashMap.put("countTrip", 1)
        Log.e("LOG", hashMap.toString())
        return hashMap

    }
}
