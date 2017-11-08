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
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_my_user_profile.*
import kotlinx.android.synthetic.main.profile_direction.*
import kotlinx.android.synthetic.main.profile_main_image.*
import ru.a1024bits.bytheway.App
import ru.a1024bits.bytheway.R
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


    private var glide: RequestManager = Glide.with(this.context)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        App.component.inject(this)
        Log.e("LOG", "function fragment Created ")
        if (arguments != null) {
            val userId: String = arguments.getString(UID_KEY)
        }
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MyProfileViewModel::class.java)

        viewModel?.user?.observe(this, Observer<User> { user ->
            Log.e("LOG", "fill Profile: $user")
            fillProfile(user)
            
        })

        viewModel!!.load(FirebaseAuth.getInstance().currentUser?.uid.orEmpty())
    }

    private fun fillProfile(user: User?) {
        username.text = StringBuilder(user?.name).append(" ").append(user?.lastName)
        city.text = user?.city




        if (user?.cities!=null) {
            var lastIndexArr = user?.cities?.size - 1
            textCityFrom.text = user?.cities?.get(0)
            textCityTo.text = user?.cities?.get(lastIndexArr)
        }

        val formatDate=SimpleDateFormat("dd.MM.yyyy")

        if (user?.dates!=null){
            var lastIndexArr = user?.dates?.size - 1
            val dayBegin=formatDate.format(Date(user?.dates[0]))
            val dayArrival=formatDate.format(Date(user?.dates[lastIndexArr]))
            textDateFrom.text=dayBegin
            textView5.text=dayArrival
        }





        glide.load(user?.urlPhoto).into(image_avatar)

        for (name in user?.socialNetwork!!) {
            when (name) {
                SocialNetwork.VK -> vkIcon.setImageResource(R.drawable.vk)
                SocialNetwork.CS -> csIcon.setImageResource(R.drawable.cs_color)
                SocialNetwork.FB -> fbcon.setImageResource(R.drawable.fb_color)
                SocialNetwork.WHATSUP -> whatsUpIcon.setImageResource(R.drawable.whats_icon__2_)
            }
        }

//        for (nameCity in user?.cities!!) {
//
//        }
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
        Log.e("LOG", "function activity create view ")

        val displayPriceTravel = view.findViewById<TextView>(R.id.display_price_travel)
        displayPriceTravel.text = StringBuilder(getString(R.string.type_money)).append(0)
        view.findViewById<SeekBar>(R.id.choose_price_travel).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                displayPriceTravel.text = StringBuilder(getString(R.string.type_money)).append(fibbonaci(p1))
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }

        })

        view.findViewById<ImageView>(R.id.vkIcon).setOnClickListener {
            openDialogVk()
        }
        view.findViewById<ImageView>(R.id.fbcon).setOnClickListener {
            openDialogDb()
        }
        view.findViewById<ImageView>(R.id.whatsUpIcon).setOnClickListener({
            openDialogWhatsUp();
        })
        view.findViewById<ImageView>(R.id.fbcon).setOnClickListener({
            openDialogFB()
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

    private fun openDialogFB() {
        val simpleAlert = AlertDialog.Builder(activity).create()
        simpleAlert.setTitle("Ссылки на социальные сети")
        simpleAlert.setMessage("Здесь вы можете указать ваши контактные данные для того что бы вас смогли найти другие путешественики")
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.custom_dialog_profile_soc_network, null)
        simpleAlert.setView(dialogView)

        simpleAlert.setButton(AlertDialog.BUTTON_POSITIVE, "Сохранить", { dialogInterface, i ->
            viewModel?.saveLinks(dialogView.findViewById<EditText>(R.id.fbEditText).text)
            Toast.makeText(activity.applicationContext, "Сохранено $'dialogView.findViewById<EditText>(R.id.fbEditText).text'", Toast.LENGTH_SHORT).show()
        })

        simpleAlert.show()

    }

    private fun openDialogWhatsUp() {

    }

    private fun openDialogDb() {

    }

    private fun openDialogVk() {
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
}// Required empty public constructor
