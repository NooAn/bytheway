package ru.a1024bits.bytheway.model


/**
 * Created by andrey.gusenkov on 18/09/2017.
 *
 */

enum class Method {
    TRAIN,
    BUS,
    CAR,
    PLANE,
    HITCHHIKING,
    BOAT
}

enum class SocialNetwork(val link: String) {
    VK(""),
    WHATSAAP(""),
    CS(""),
    FB(""),
    TG("")
}

data class User(var name: String = "",
                var lastName: String = "",
                var age: Long = 0,
                var id: String = "0",
                var email: String = "",
                var phone: String = "",
                var countTrip: Int = 0,
                var route: ArrayList<String> = arrayListOf(),
                var cities: ArrayList<String> = arrayListOf(),
                var method: ArrayList<Method> = arrayListOf(),
                var dates: ArrayList<Long> = arrayListOf(),
                var budget: Long = 0,
                var city: String = "",
                var percentsSimilarTravel: Int = 0,
                var addInformation: String = "",
                var sex: Int = 0,
                var socialNetwork: ArrayList<SocialNetwork> = arrayListOf(SocialNetwork.VK, SocialNetwork.FB),
                var data: Long = 0,
                var urlPhoto: String = "https://www.ischool.berkeley.edu/sites/default/files/default_images/avatar.jpeg") {
}
