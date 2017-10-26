package ru.a1024bits.bytheway.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AviaCity implements Parcelable {
    @SerializedName("index_strings")
    private List<String> indexStrings = null;
    @SerializedName("type")
    private String type;
    @SerializedName("coordinates")
    private List<Double> location;
    @SerializedName("location")
    private Coordinates coordinates;
    private Object stateCode;
    @SerializedName("name")
    private String name;
    @SerializedName("countryCode")
    private String countryCode;
    private Integer weight;
    @SerializedName("code")
    private String code;
    @SerializedName("country_name")
    private String countryName;
    @SerializedName("airport_name")
    private String airportName;
    @SerializedName("city_name")
    private String cityName;
    @SerializedName("city_code")
    private String cityCode;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    public final static Parcelable.Creator<AviaCity> CREATOR = new Creator<AviaCity>() {


        @SuppressWarnings({
                "unchecked"
        })
        public AviaCity createFromParcel(Parcel in) {
            return new AviaCity(in);
        }

        public AviaCity[] newArray(int size) {
            return (new AviaCity[size]);
        }

    };

    protected AviaCity(Parcel in) {
        in.readList(this.indexStrings, (java.lang.String.class.getClassLoader()));
        this.type = ((String) in.readValue((String.class.getClassLoader())));
        this.coordinates = ((Coordinates) in.readValue((Coordinates.class.getClassLoader())));
        this.stateCode = ((Object) in.readValue((Object.class.getClassLoader())));
        this.name = ((String) in.readValue((String.class.getClassLoader())));
        this.countryCode = ((String) in.readValue((String.class.getClassLoader())));
        this.weight = ((Integer) in.readValue((Integer.class.getClassLoader())));
        this.code = ((String) in.readValue((String.class.getClassLoader())));
        this.countryName = ((String) in.readValue((String.class.getClassLoader())));
        this.cityName = ((String) in.readValue((String.class.getClassLoader())));
        this.cityCode = ((String) in.readValue((String.class.getClassLoader())));
        this.additionalProperties = ((Map<String, Object>) in.readValue((Map.class.getClassLoader())));
    }

    public AviaCity() {
    }

    public List<String> getIndexStrings() {
        return indexStrings;
    }

    public void setIndexStrings(List<String> indexStrings) {
        this.indexStrings = indexStrings;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public Object getStateCode() {
        return stateCode;
    }

    public void setStateCode(Object stateCode) {
        this.stateCode = stateCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(indexStrings);
        dest.writeValue(type);
        dest.writeValue(coordinates);
        dest.writeValue(stateCode);
        dest.writeValue(name);
        dest.writeValue(countryCode);
        dest.writeValue(weight);
        dest.writeValue(code);
        dest.writeValue(countryName);
        dest.writeValue(cityName);
        dest.writeValue(cityCode);
        dest.writeValue(additionalProperties);
    }

    public int describeContents() {
        return 0;
    }

    public String getAirportName() {
        return airportName;
    }

    public void setAirportName(String airportName) {
        this.airportName = airportName;
    }

    public List<Double> getLocation() {
        return location;
    }

    public void setLocation(List<Double> location) {
        this.location = location;
    }
}