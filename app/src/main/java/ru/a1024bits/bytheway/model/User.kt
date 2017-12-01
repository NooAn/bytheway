package ru.a1024bits.bytheway.model

enum class Method {
    TRAIN,
    BUS,
    CAR,
    PLANE,
    HITCHHIKING,
    BOAT
}

enum class SocialNetwork(var link: String) {
    VK("VK"),
    WHATSAAP("WHATSAAP"),
    CS("CS"),
    FB("FB"),
    TG("TG")
}

data class AirInfo(var hours: String = "0", var countries: String = "0", var kilometers: String = "0")
/**
 *
 */
data class User(var name: String = "",
                var lastName: String = "",
                var age: Long = 0,
                var id: String = "0",
                var email: String = "",
                var phone: String = "",
                var countTrip: Int = 0,
                var airInfo: AirInfo = AirInfo(),
                var flightHours: Long = 0,
                var countries: Long = 0,
                var kilometers: Long = 0,
                var route: String = "",//ArrayList<String> = arrayListOf(),
                var cities: ArrayList<String> = arrayListOf(),
                var method: ArrayList<Method> = arrayListOf(),
                var dates: ArrayList<Long> = arrayListOf(),
                var budget: Long = 0,
                var city: String = "",
                var percentsSimilarTravel: Int = 0,
                var addInformation: String = "",
                var sex: Int = 0,
                var socialNetwork: HashMap<String, String> = hashMapOf<String, String>(),
                var data: Long = 0,
                var urlPhoto: String = "https://www.ischool.berkeley.edu/sites/default/files/default_images/avatar.jpeg") {
}
