package ru.a1024bits.aviaanimation.ui.util

/**
 * Created by x220 on 01.10.2017.
 */

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.annotation.TargetApi
import android.os.Build
import android.util.Log
import android.util.Property
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker


class MarkerAnimation {
    var flag: Boolean = false
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    internal fun animateMarker(marker: Marker?, startPosition: LatLng, finalPosition: LatLng, latLngInterpolator: LatLngInterpolator, onAnimationEnd: () -> Unit = {}) {
        val typeEvaluator = TypeEvaluator<LatLng> { fraction, startValue, endValue ->
            latLngInterpolator.interpolate(fraction, startValue, endValue)
        }

        val property = Property.of(Marker::class.java, LatLng::class.java, "position")
        val animator = ObjectAnimator.ofObject(marker, property, typeEvaluator, finalPosition)
        animator.addUpdateListener({ animation ->
            try {
                val v = animation.animatedFraction
                val newPosition = latLngInterpolator.interpolate(v, startPosition, finalPosition)
                marker?.setRotation(getBearing(marker.position, newPosition))
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        })
        animator.duration = 5000
        animator.setAutoCancel(true)
        flag = true;
        animator.start()
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                onAnimationEnd.invoke()
            }
        })

    }

    //Method for finding bearing between two points
    fun getBearing(begin: LatLng, end: LatLng): Float {
        val lat = Math.abs(begin.latitude - end.latitude)
        val lng = Math.abs(begin.longitude - end.longitude)

        if (begin.latitude < end.latitude && begin.longitude < end.longitude)
            return Math.toDegrees(Math.atan(lng / lat)).toFloat()
        else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)
            return (90 - Math.toDegrees(Math.atan(lng / lat)) + 90).toFloat()
        else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)
            return (Math.toDegrees(Math.atan(lng / lat)) + 180).toFloat()
        else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)
            return (90 - Math.toDegrees(Math.atan(lng / lat)) + 270).toFloat()
        return -1f
    }
}