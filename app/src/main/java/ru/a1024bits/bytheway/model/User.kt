package ru.a1024bits.bytheway.model

import android.os.Parcel
import android.os.Parcelable


/**
 * Created by andrey.gusenkov on 18/09/2017.
 */
data class User(val name: String, val lastName: String = "", val age: Int = 0) : Parcelable{
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readInt())

    override fun writeToParcel(p0: Parcel, p1: Int) {
        p0.writeString(name)
        p0.writeString(lastName)
        p0.writeInt(age)
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
