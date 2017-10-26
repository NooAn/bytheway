package ru.a1024bits.aviaanimation.ui.util

/**
 * This class from https://gist.github.com/broady/6314689
 */

import com.google.android.gms.maps.model.LatLng

interface LatLngInterpolator {
    fun interpolate(fraction: Float, fromLocation: LatLng, endLocation: LatLng): LatLng

    class Linear : LatLngInterpolator {
        override fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng {
            val lat = (b.latitude - a.latitude) * fraction + a.latitude
            val lng = (b.longitude - a.longitude) * fraction + a.longitude
            return LatLng(lat, lng)
        }
    }


    class CurveBezie() : LatLngInterpolator {
        override fun interpolate(fraction: Float, fromLocation: LatLng, endLocation: LatLng): LatLng =
                calculateBezierFunction(fraction.toDouble(), fromLocation, endLocation)

        fun calculateBezierFunction(t: Double, fromLocation: LatLng, endLocation: LatLng): LatLng {
            val d = (Math.abs(endLocation.latitude) - Math.abs(fromLocation.latitude)) / 2
            val c = (Math.abs(endLocation.longitude) - Math.abs(fromLocation.longitude)) / 2

            var point2: LatLng
            var point3: LatLng

            if (Math.abs(c) > Math.abs(d)) {
                point2 = LatLng(endLocation.latitude + c * (3 / d), endLocation.longitude - c)
                point3 = LatLng(fromLocation.latitude - c * (2 / d), fromLocation.longitude + c)
            } else {
                point2 = LatLng(endLocation.latitude - d, endLocation.longitude + d * (3 / d))
                point3 = LatLng(fromLocation.latitude + d, fromLocation.longitude - d * (3 / d))
            }
            //  x == lat, y == lon
            val a1 = fromLocation.latitude * Math.pow(1 - t, 3.0)
            val b1 = 3 * point2.latitude * t * (Math.pow(t, 2.0) - 2 * t + 1)
            val c1 = 3 * point3.latitude * Math.pow(t, 2.0) * (1 - t)
            val d1 = endLocation.latitude * Math.pow(t, 3.0)
            val x = a1 + b1 + c1 + d1

            val a2 = fromLocation.longitude * Math.pow(1 - t, 3.0)
            val b2 = 3 * point2.longitude * t * (Math.pow(t, 2.0) - 2 * t + 1)
            val c2 = 3 * point3.longitude * Math.pow(t, 2.0) * (1 - t)
            val d2 = endLocation.longitude * Math.pow(t, 3.0)
            val y = a2 + b2 + c2 + d2
            return LatLng(x, y);
        }

    }


}