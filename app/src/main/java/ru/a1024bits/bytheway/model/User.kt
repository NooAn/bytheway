package ru.a1024bits.bytheway.model

import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

enum class Method(var link: String) {
    TRAIN("train"),
    BUS("bus"),
    CAR("car"),
    PLANE("plane"),
    HITCHHIKING("hitchhiking")
    //BOAT("boat")
}

enum class SocialNetwork(var link: String) {
    VK("VK"),
    WHATSAPP("WHATSAPP"),
    CS("CS"),
    FB("FB"),
    TG("TG")
}

class SocialResponse(var link: String = "", var value: String = "")

class FireBaseNotification(var title: String = "",
                           var body: String = "",
                           var cmd: String = "",
                           var value: String? = "")
/**
 *
 */
data class User(var name: String = "",
                var lastName: String = "",
                var age: Int = 0,
                var id: String = "0",
                var email: String = "",
                var phone: String = "",
                var countTrip: Int = 0,
                var flightHours: Long = 0,
                var cityFromLatLng: GeoPoint = GeoPoint(0.0, 0.0),
                var cityToLatLng: GeoPoint = GeoPoint(0.0, 0.0),
                var cityTwoLatLng: GeoPoint = GeoPoint(0.0, 0.0),
                var countries: Long = 0,
                var kilometers: Long = 0,
                var route: String = "",
                var cities: HashMap<String, String> = hashMapOf<String, String>(),
                var method: HashMap<String, Boolean> = hashMapOf(Method.BUS.link to false,
                        Method.TRAIN.link to false,
                        Method.PLANE.link to false,
                        Method.CAR.link to false,
                        Method.HITCHHIKING.link to false),
                var dates: HashMap<String, Long> = hashMapOf<String, Long>(),
                var budget: Long = 0,
                var budgetPosition: Int = 0,
                var city: String = "",
                var percentsSimilarTravel: Int = 0,
                var addInformation: String = "",
                var sex: Int = 0,
                var socialNetwork: HashMap<String, String> = hashMapOf<String, String>(),
                var data: Long = 0,
                var token: String = "",
                var urlPhoto: String = "https://www.ischool.berkeley.edu/sites/default/files/default_images/avatar.jpeg",
                @ServerTimestamp var timestamp: Date? = Date())
