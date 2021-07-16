package com.ggi.uparty.other;

import com.google.android.gms.maps.model.LatLng;

//Class to contain the city, state, and country info for a location with a toString() method
// to spit out the location in the right format for the database
public class LocationItem {
    public String city;
    public String state;
    public String country;
    public String placeKey;
    public String placeName;
    public LatLng placeLatLng;
    public LocationItem(){
        city = "";
        state = "";
        country = "";
    }
    public LocationItem(String city, String state, String country){
        this.city = city;
        this.state = state;
        this.country = country;
    }
    public String toString(){
        if(city == null || state == null || country == null){
            return "";
        }
        if(city.isEmpty() || state.isEmpty() || country.isEmpty()){
            return "";
        }else{
            return city + "@" + state +"@" + country;
        }
    }
}
