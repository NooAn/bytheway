package ru.a1024bits.bytheway.model

import android.os.Parcel
import android.os.Parcelable


/**
 * Created by andrey.gusenkov on 18/09/2017.
 */
data class User(var name: String = "", var lastName: String = "", var age: Long = 0,
                val urlPhoto: String = "https://www.ischool.berkeley.edu/sites/default/files/default_images/avatar.jpeg",
                var percentsSimilarTravel: Int) : Parcelable {

    constructor(name: String = "", lastName: String = "", age: Long = 0,
                urlPhoto: String = "https://www.ischool.berkeley.edu/sites/default/files/default_images/avatar.jpeg"     ) :
            this(name, lastName, age, urlPhoto, 0)

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readLong(),
            parcel.readString(),
            parcel.readInt())

    override fun writeToParcel(p0: Parcel, p1: Int) {
        p0.writeString(name)
        p0.writeString(lastName)
        p0.writeLong(age)
        p0.writeString(urlPhoto)
        p0.writeInt(percentsSimilarTravel)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }

}
