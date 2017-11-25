package ru.a1024bits.bytheway.ui.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_app_in_the_air_sinchronized.*
import ru.a1024bits.bytheway.App
import ru.a1024bits.bytheway.R
import ru.a1024bits.bytheway.router.Screens
import ru.a1024bits.bytheway.ui.activity.MenuActivity
import ru.terrakok.cicerone.commands.Replace

/**
 * Created by x220 on 25.11.2017.
 */
class AppInTheAirSinchronizedFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_app_in_the_air_sinchronized, container, false)
        return view;
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button_miss.setOnClickListener {
            //return in menu
            (activity as MenuActivity).navigator.applyCommand(Replace(Screens.MY_PROFILE_SCREEN, 1))
        }
        button_sinch.setOnClickListener {
            //open app_in_the_air
            (activity as MenuActivity).navigator.applyCommand(Replace(Screens.LOGIN_APP_IN_THE_AIR, 1))
            login()
        }
    }

    private fun login() {
        
    }
}