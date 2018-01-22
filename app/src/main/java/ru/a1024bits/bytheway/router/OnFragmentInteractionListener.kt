package ru.a1024bits.bytheway.router

import com.google.android.gms.maps.model.LatLng
import ru.a1024bits.bytheway.model.User

/**
 * Created by x220 on 29.10.2017.
 */
interface OnFragmentInteractionListener {
    fun onFragmentInteraction(user: User?)
    fun onSetPoint(l: LatLng, pos: Int, swap: Boolean = false)
}