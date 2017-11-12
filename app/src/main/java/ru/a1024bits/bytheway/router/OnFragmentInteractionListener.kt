package ru.a1024bits.bytheway.router

import com.google.android.gms.maps.model.LatLng

/**
 * Created by x220 on 29.10.2017.
 */
interface OnFragmentInteractionListener {
    fun onFragmentInteraction()
    fun onSetPoint(l: LatLng, pos: Int)
}