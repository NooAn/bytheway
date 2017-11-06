package ru.a1024bits.bytheway.model

import android.os.Parcel
import android.os.Parcelable


/**
 * Created by andrey.gusenkov on 18/09/2017.
 *
 */
//route - city A - city B
//map
//date
//method
//budget
//add_info
//name and last_name and age and sex!
//social_network

enum class Method {
    TRAIN,
    BUS,
    CAR,
    PLANE,
    HITCHHIKING,
    BOAT
}

enum class SocialNetwork {
    VK,
    WHATSUP,
    CS,
    FB
}

data class User(var name: String = "",
                var lastName: String = "",
                var age: Long = 0,
                var route: ArrayList<String> = arrayListOf(),
                var cities: ArrayList<String> = arrayListOf(),
                var method: ArrayList<Method> = arrayListOf(),
                var budget: Long = 0,
                var city: String = "",
                var percentsSimilarTravel: Int = 0,
                var addInformation: String = "",
                var sex: Int = 0,
                var socialNetwork: ArrayList<SocialNetwork> = arrayListOf(SocialNetwork.VK, SocialNetwork.FB),
                var data: Long = 0,
                var urlPhoto: String = "https://www.ischool.berkeley.edu/sites/default/files/default_images/avatar.jpeg") {

//    constructor(parcel: Parcel) : this(
//            parcel.readString(),
//            parcel.readString(),
//            parcel.readLong(),
//            parcel.readString())
//
//    override fun writeToParcel(p0: Parcel, p1: Int) {
//        p0.writeString(name)
//        p0.writeString(lastName)
//        p0.writeLong(age)
//        p0.writeString(urlPhoto)
//    }
//
//    override fun describeContents(): Int = 0
//
//    companion object CREATOR : Parcelable.Creator<User> {
//        override fun createFromParcel(parcel: Parcel): User {
//            return User(parcel)
//        }
//
//        override fun newArray(size: Int): Array<User?> {
//            return arrayOfNulls(size)
//        }
//    }

}
