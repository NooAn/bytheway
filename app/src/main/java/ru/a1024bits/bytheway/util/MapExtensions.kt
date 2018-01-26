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

fun LatLng.getBearing(end: LatLng): Float {
    val lat = Math.abs(this.latitude - end.latitude)
    val lng = Math.abs(this.longitude - end.longitude)
    if (this.latitude < end.latitude && this.longitude < end.longitude)
        return Math.toDegrees(Math.atan(lng / lat)).toFloat()
    else if (this.latitude >= end.latitude && this.longitude < end.longitude)
        return (90 - Math.toDegrees(Math.atan(lng / lat)) + 90).toFloat()
    else if (this.latitude >= end.latitude && this.longitude >= end.longitude)
        return (Math.toDegrees(Math.atan(lng / lat)) + 180).toFloat()
    else if (this.latitude < end.latitude && this.longitude >= end.longitude)
        return (90 - Math.toDegrees(Math.atan(lng / lat)) + 270).toFloat()
    return -1f

}
