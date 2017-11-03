package ru.a1024bits.bytheway.model

import android.os.Parcel
import android.os.Parcelable


/**
 * Created by andrey.gusenkov on 18/09/2017.
 */
data class User(var name: String = "", var lastName: String = "", var age: Int = 0, val urlPhoto: String = "https://www.ischool.berkeley.edu/sites/default/files/default_images/avatar.jpeg") : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readString())
    
    override fun writeToParcel(p0: Parcel, p1: Int) {
        p0.writeString(name)
        p0.writeString(lastName)
        p0.writeInt(age)
        p0.writeString(urlPhoto)
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
