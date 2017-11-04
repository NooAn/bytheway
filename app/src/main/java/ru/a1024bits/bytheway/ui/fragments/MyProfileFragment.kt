package ru.a1024bits.bytheway.ui.fragments

import android.arch.lifecycle.LifecycleFragment
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.borax12.materialdaterangepicker.date.DatePickerDialog
import com.borax12.materialdaterangepicker.time.RadialPickerLayout
import com.borax12.materialdaterangepicker.time.TimePickerDialog
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.model.User
import ru.a1024bits.bytheway.router.OnFragmentInteractionListener
import ru.a1024bits.bytheway.viewmodel.UserProfileViewModel
import java.util.*
import kotlin.collections.HashMap


class MyProfileFragment : LifecycleFragment(), OnMapReadyCallback, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    
    
    private var viewModel: UserProfileViewModel? = null
    
    private var mListener: OnFragmentInteractionListener? = null
    
    var db = FirebaseFirestore.getInstance()
    val TAG = "MyProfileFragment"
    
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
    }
    
    
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.e("LOG", "function fragment Created ")
        if (arguments != null) {
            Log.e("LOG", "enter")
            val userId: String = arguments.getString(UID_KEY)
            viewModel = ViewModelProviders.of(this).get(UserProfileViewModel::class.java)
            viewModel?.init(userId)
            viewModel?.user?.observe(this, Observer {
                Log.e("LOG", "observer only")
            })
            viewModel?.user?.observe(this, object : Observer<User> {
                override fun onChanged(t: User?) {
                    Log.e("LOG", "onChanged")
                }
            })
        }
        
        val user = HashMap<String, Any>()
        user.put("name", "Ada")
        user.put("last_name", "Lovelace")
        user.put("age", 1815)
        
        Log.e("LOG", db.app.uid + db.app.name + " " + db.firestoreSettings.host)
        
        // Add a new document with a generated ID
        db.collection("users")
                .add(user)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.id)
                }
                .addOnFailureListener { e -> Log.w(TAG, "Error adding document", e) }
        
        db.collection("users")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result) {
                            Log.d(TAG, document.id + " => " + document.data)
                        }
                    } else {
                        Log.w(TAG, "Error getting documents.", task.exception)
                    }
                }
    }
    
    override fun onResume() {
        super.onResume()
        mMapView?.onResume()
//        val dpd = activity.fragmentManager.findFragmentByTag("Datepickerdialog") as DatePickerDialog
//        dpd?.setOnDateSetListener(this)
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
        mMapView = view?.findViewById<MapView>(R.id.mapView)
        val datetrip = view?.findViewById<TextView>(R.id.choice_date)
        mMapView?.onCreate(savedInstanceState)
        
        mMapView?.onResume()// needed to get the map to display immediately
        
        try {
            MapsInitializer.initialize(activity.applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        settingsSocialNetworkButtons()
        
        mMapView?.getMapAsync(this)
        
        
        datetrip?.setOnClickListener({
            val now = Calendar.getInstance()
            val dateDialog = com.borax12.materialdaterangepicker.date.DatePickerDialog.newInstance(
                    this@MyProfileFragment,
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            )
            dateDialog.isAutoHighlight = true
            dateDialog.show(activity.fragmentManager, "Datepickerdialog")
        })
        
        return view;
    }
    
    override fun onDateSet(view: DatePickerDialog, year: Int, monthOfYear: Int, dayOfMonth: Int, yearEnd: Int, monthOfYearEnd: Int, dayOfMonthEnd: Int) {
        var monthOfYear = monthOfYear
        var monthOfYearEnd = monthOfYearEnd
        val date = "You picked the following date: From- " + dayOfMonth + "/" + ++monthOfYear + "/" + year + " To " + dayOfMonthEnd + "/" + ++monthOfYearEnd + "/" + yearEnd
        Log.e("LOG date:", "date:" + date);
    }
    
    override fun onTimeSet(view: RadialPickerLayout?, hourOfDay: Int, minute: Int, hourOfDayEnd: Int, minuteEnd: Int) {
    
    }
    
    private fun settingsSocialNetworkButtons() {
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
    
    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
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
