package ru.a1024bits.bytheway.model;

/**
 * Created by x220 on 30.09.2017.
 */

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Coordinates implements Parcelable {

    @SerializedName("lat")
    private double lat;
    @SerializedName("lon")
    private double lon;

    public final static Parcelable.Creator<Coordinates> CREATOR = new Creator<Coordinates>() {
        public Coordinates createFromParcel(Parcel in) {
            return new Coordinates(in);
        }

        public Coordinates[] newArray(int size) {
            return (new Coordinates[size]);
        }

    };

    protected Coordinates(Parcel in) {
        this.lat = ((double) in.readValue((double.class.getClassLoader())));
        this.lon = ((double) in.readValue((double.class.getClassLoader())));
    }

    public Coordinates() {
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(lat);
        dest.writeValue(lon);
    }

    public int describeContents() {
        return 0;
    }

}
