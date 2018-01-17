package ru.a1024bits.bytheway.ui.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_air_succesfull.*

import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.router.OnFragmentInteractionListener
import ru.a1024bits.bytheway.router.Screens
import ru.a1024bits.bytheway.ui.activity.MenuActivity
import ru.terrakok.cicerone.commands.Replace
import android.content.Intent
import android.util.Log
import android.widget.TextView
import com.google.firebase.analytics.FirebaseAnalytics


class AirSuccesfullFragment : Fragment() {

    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    lateinit var mFirebaseAnalytics: FirebaseAnalytics

    private val MIN_LENGTH_AIRPORT: Int  = 1

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this.context)
        val view: View?

        Log.e("LOG", "le ${arguments.getString(NAME).length} name:${arguments.getString(NAME)}")

        if (arguments != null && arguments.getString(NAME).length > MIN_LENGTH_AIRPORT) {
            view = inflater?.inflate(R.layout.fragment_air_succesfull, container, false)
            mFirebaseAnalytics.setCurrentScreen(this.activity, "AppInTheAir_succesfull_sinch", this.javaClass.simpleName)
        } else {
            mFirebaseAnalytics.setCurrentScreen(this.activity, "AppInTheAir_failed_sinch", this.javaClass.simpleName)
            view = inflater?.inflate(R.layout.fragment_air_disaster, container, false)
        }

        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view?.findViewById<View>(R.id.button_miss)?.setOnClickListener {
            //return in menu // change on the Router
            (activity as MenuActivity).navigator.applyCommand(Replace(Screens.MY_PROFILE_SCREEN, 1))
        }
        view?.findViewById<View>(R.id.app_in_the_air_link)?.setOnClickListener {
            val url = "https://www.appintheair.mobi"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
            mFirebaseAnalytics.logEvent("AppInTheAir_common_look_link", null)
        }
        if (arguments != null && arguments.getString(NAME).length > MIN_LENGTH_AIRPORT) {
            view?.findViewById<TextView>(R.id.city_from_air)?.text = arguments.getString(NAME)
            mFirebaseAnalytics.logEvent("AppInTheAir_common", arguments)
            if (view?.findViewById<TextView>(R.id.date_air_go) != null) view.findViewById<TextView>(R.id.date_air_go).text = arguments.getString(DATE)
        }
    }

    fun onButtonPressed(uri: Uri) {
        if (mListener != null) {

        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context?.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }


    companion object {
        private val NAME = "name_city"
        private val DATE = "date_trip"

        fun newInstance(name: String, date: String): AirSuccesfullFragment {
            val fragment = AirSuccesfullFragment()
            val args = Bundle()
            args.putString(NAME, name)
            args.putString(DATE, date)
            fragment.arguments = args
            return fragment
        }
    }
}
