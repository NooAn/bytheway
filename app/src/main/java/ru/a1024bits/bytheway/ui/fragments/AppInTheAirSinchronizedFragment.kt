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
import ru.a1024bits.bytheway.util.ServiceGenerator
import android.content.Intent
import android.net.Uri
import ru.a1024bits.bytheway.util.API_BASE_URL


/**
 * Created by x220 on 25.11.2017.
 */
const val clientId = "38fee1c0-7e1b-4afc-9413-8fe0e1986df8"
const val clientSecret = "WrqxeV6i-8wRvxxlWXVlYNWy!NZrX3ArWGv*wmftaQ_pkV*8Cn!0.3fou539f)ZwiF8\$*7IM.UufrNZ-UWgB"
const val redirectUri = "https://www.appintheair.mobi/blank"

class AppInTheAirSinchronizedFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_app_in_the_air_sinchronized, container, false)
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
            login()
        }
    }

    private fun login() {
        val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(API_BASE_URL + "/oauth/authorize" + "?client_id=" + clientId + "&redirect_uri=" + redirectUri +"&response_type=code&scope=user_info%20user_flights%20user_email" ))
        startActivity(intent)
    }
}