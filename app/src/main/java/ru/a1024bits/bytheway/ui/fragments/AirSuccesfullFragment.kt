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


class AirSuccesfullFragment : Fragment() {

    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view: View?
        if (arguments != null && arguments.getString(NAME).length > 0)
            view = inflater!!.inflate(R.layout.fragment_air_succesfull, container, false)
        else view = inflater!!.inflate(R.layout.fragment_air_disaster, container, false)

        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button_miss.setOnClickListener {
            //return in menu
            (activity as MenuActivity).navigator.applyCommand(Replace(Screens.MY_PROFILE_SCREEN, 1))
        }
        if (arguments != null) {
            city_from_air.text = arguments.getString(NAME);
            date_air_go.text = arguments.getString(DATE);
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
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
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
