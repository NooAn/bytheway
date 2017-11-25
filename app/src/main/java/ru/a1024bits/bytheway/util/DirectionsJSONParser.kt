package ru.a1024bits.bytheway.util

import com.google.android.gms.maps.model.LatLng
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject



/**
 * Created by tikhon.osipov on 26.11.17.
 */
class DirectionsJSONParser {
    /** Receives a JSONObject and returns a list of lists containing latitude and longitude  */
    fun parse(jObject: JSONObject): List<List<HashMap<String, String>>> {

        val routes = ArrayList<List<HashMap<String, String>>>()
        var jRoutes: JSONArray? = null
        var jLegs: JSONArray? = null
        var jSteps: JSONArray? = null

        try {

            jRoutes = jObject.getJSONArray("routes")

            /** Traversing all routes  */
            for (i in 0 until jRoutes!!.length()) {
                jLegs = (jRoutes.get(i) as JSONObject).getJSONArray("legs")
                val path = ArrayList<HashMap<String, String>>()

                /** Traversing all legs  */
                for (j in 0 until jLegs!!.length()) {
                    jSteps = (jLegs.get(j) as JSONObject).getJSONArray("steps")

                    /** Traversing all steps  */
                    for (k in 0 until jSteps!!.length()) {
                        var polyline = ""
                        polyline = ((jSteps.get(k) as JSONObject).get("polyline") as JSONObject).get("points") as String
                        val list = decodePoly(polyline)

                        /** Traversing all points  */
                        for (l in list.indices) {
                            val hm = HashMap<String, String>()
                            hm.put("lat", java.lang.Double.toString(list[l].latitude))
                            hm.put("lng", java.lang.Double.toString(list[l].longitude))
                            path.add(hm)
                        }
                    }
                    routes.add(path)
                }
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e: Exception) {
        }

        return routes
    }

    /**
     * Method to decode polyline points
     * Courtesy : http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     */
    private fun decodePoly(encoded: String): List<LatLng> {

        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = LatLng(lat.toDouble() / 1E5,
                    lng.toDouble() / 1E5)
            poly.add(p)
        }

        return poly
    }
}