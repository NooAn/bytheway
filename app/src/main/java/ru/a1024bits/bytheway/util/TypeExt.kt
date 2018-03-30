package ru.a1024bits.bytheway.util

import android.text.Editable
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import java.text.SimpleDateFormat
import java.util.*


fun String.joinToString(s: String): String {
    return StringBuilder(this).append(s).toString()
}

fun String.isNumberPhone(): Boolean {
    return this.matches(Regex("^([0-9]|\\+[0-9]){11,13}\$"))
    //this.startsWith("+7")
}

fun String.getIntOrNothing(): Int = if (this.isBlank()) 0 else this.toInt()


val Long.toStringOrEmpty: String
    get() {
        return if (this == 0L) "" else this.toString()
    }

fun Long.getNormallDate(): CharSequence? = SimpleDateFormat("dd.MM.yyyy", Locale.US).format(this)

fun GeoPoint.toLatLng(): LatLng? = LatLng(this.latitude, this.longitude)

fun Int.toStringOrBlank(): String = if (this == 0) "" else this.toString()

fun Editable.toStringOrZero(): String = if (this.toString().isBlank()) "0" else this.toString()
