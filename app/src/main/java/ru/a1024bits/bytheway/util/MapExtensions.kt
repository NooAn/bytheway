package ru.a1024bits.bytheway.util

import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import ru.a1024bits.bytheway.R

/**
 * Created by tikhon.osipov on 25.11.17
 */

fun LatLng.createMarker(title: String): MarkerOptions =
        MarkerOptions()
                .position(this)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_blue))
                .title(title)

fun LatLng.toJsonString(): String = "${this.latitude},${this.longitude}"
